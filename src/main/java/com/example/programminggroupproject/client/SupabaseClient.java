package com.example.programminggroupproject.client;

import com.example.programminggroupproject.config.SupabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Supabase HTTP client for REST API operations.
 * Handles all HTTP communication with Supabase PostgREST API.
 */
public class SupabaseClient {
    
    private static SupabaseClient instance;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SupabaseConfig config;
    
    private SupabaseClient() {
        this.config = SupabaseConfig.getInstance();
        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(new SupabaseInterceptor())
                .build();
        
        // Configure ObjectMapper for JSON serialization
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }
    
    /**
     * Interceptor to add authentication headers to all requests
     */
    private class SupabaseInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .addHeader("apikey", config.getApiKey())
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build();
            return chain.proceed(request);
        }
    }
    
    /**
     * Execute a GET request
     */
    public String get(String table, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(config.getRestUrl() + "/" + table).newBuilder();
        
        if (params != null) {
            params.forEach(urlBuilder::addQueryParameter);
        }
        
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * Execute a GET request with count header
     */
    public Response getWithCount(String table, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(config.getRestUrl() + "/" + table).newBuilder();
        
        if (params != null) {
            params.forEach(urlBuilder::addQueryParameter);
        }
        
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Prefer", "count=exact")
                .get()
                .build();
        
        return httpClient.newCall(request).execute();
    }
    
    /**
     * Execute a POST request (INSERT)
     */
    public String post(String table, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(config.getRestUrl() + "/" + table)
                .post(body)
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * Execute a PATCH request (UPDATE)
     */
    public String patch(String table, String jsonBody, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(config.getRestUrl() + "/" + table).newBuilder();
        
        if (params != null) {
            params.forEach(urlBuilder::addQueryParameter);
        }
        
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .patch(body)
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * Execute a DELETE request
     */
    public void delete(String table, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(config.getRestUrl() + "/" + table).newBuilder();
        
        if (params != null) {
            params.forEach(urlBuilder::addQueryParameter);
        }
        
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .delete()
                .build();
        
        executeRequest(request);
    }
    
    /**
     * Execute a POST request with upsert preference
     */
    public String upsert(String table, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(config.getRestUrl() + "/" + table)
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(body)
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * Execute request and return response body as string
     */
    private String executeRequest(Request request) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("Supabase request failed: " + response.code() + " - " + errorBody);
            }
            
            if (response.body() == null) {
                throw new IOException("Empty response body");
            }
            
            return response.body().string();
        }
    }
    
    /**
     * Get ObjectMapper instance for JSON serialization/deserialization
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * Helper method to build query parameters for filters
     */
    public static Map<String, String> buildParams() {
        return new HashMap<>();
    }
}
