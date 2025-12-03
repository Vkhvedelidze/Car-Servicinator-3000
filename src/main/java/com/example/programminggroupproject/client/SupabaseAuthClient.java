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
 * Supabase Authentication API client.
 * Handles user authentication, registration, and session management using Supabase Auth.
 */
public class SupabaseAuthClient {
    
    private static SupabaseAuthClient instance;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SupabaseConfig config;
    
    private SupabaseAuthClient() {
        this.config = SupabaseConfig.getInstance();
        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
        
        // Configure ObjectMapper for JSON serialization
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    public static synchronized SupabaseAuthClient getInstance() {
        if (instance == null) {
            instance = new SupabaseAuthClient();
        }
        return instance;
    }
    
    /**
     * Interceptor to add authentication headers to all Auth API requests
     */
    private class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .addHeader("apikey", config.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .build();
            return chain.proceed(request);
        }
    }
    
    /**
     * Sign up a new user with email and password
     * @param email User's email
     * @param password User's password
     * @param metadata Additional user metadata (full_name, role, shop_id)
     * @return JSON response from Supabase Auth
     * @throws IOException if the request fails
     */
    public String signUp(String email, String password, Map<String, Object> metadata) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        
        if (metadata != null && !metadata.isEmpty()) {
            body.put("data", metadata);
        }
        
        String jsonBody = objectMapper.writeValueAsString(body);
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(config.getAuthUrl() + "/signup")
                .post(requestBody)
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * Sign in an existing user with email and password
     * @param email User's email
     * @param password User's password
     * @return JSON response containing access token, refresh token, and user data
     * @throws IOException if the request fails
     */
    public String signIn(String email, String password) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        
        String jsonBody = objectMapper.writeValueAsString(body);
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(config.getAuthUrl() + "/token?grant_type=password")
                .post(requestBody)
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * Sign out the current user
     * @param accessToken The user's access token
     * @throws IOException if the request fails
     */
    public void signOut(String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url(config.getAuthUrl() + "/logout")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();
        
        executeRequest(request);
    }
    
    /**
     * Refresh the user's session using a refresh token
     * @param refreshToken The refresh token
     * @return JSON response containing new access token and refresh token
     * @throws IOException if the request fails
     */
    public String refreshSession(String refreshToken) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("refresh_token", refreshToken);
        
        String jsonBody = objectMapper.writeValueAsString(body);
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(config.getAuthUrl() + "/token?grant_type=refresh_token")
                .post(requestBody)
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * Get the current user's information using an access token
     * @param accessToken The user's access token
     * @return JSON response containing user data
     * @throws IOException if the request fails
     */
    public String getUser(String accessToken) throws IOException {
        Request request = new Request.Builder()
                .url(config.getAuthUrl() + "/user")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * Update user metadata
     * @param accessToken The user's access token
     * @param metadata User metadata to update
     * @return JSON response containing updated user data
     * @throws IOException if the request fails
     */
    public String updateUser(String accessToken, Map<String, Object> metadata) throws IOException {
        Map<String, Object> body = new HashMap<>();
        if (metadata != null && !metadata.isEmpty()) {
            body.put("data", metadata);
        }
        
        String jsonBody = objectMapper.writeValueAsString(body);
        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        
        Request request = new Request.Builder()
                .url(config.getAuthUrl() + "/user")
                .addHeader("Authorization", "Bearer " + accessToken)
                .put(requestBody)
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * Execute request and return response body as string
     */
    private String executeRequest(Request request) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                throw new IOException("Supabase Auth request failed [" + response.code() + "]: " + responseBody);
            }
            
            return responseBody;
        }
    }
    
    /**
     * Get ObjectMapper instance for JSON serialization/deserialization
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}

