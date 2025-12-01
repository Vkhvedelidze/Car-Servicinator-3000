package com.example.programminggroupproject.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for Supabase credentials.
 * Loads credentials from supabase.properties file.
 */
public class SupabaseConfig {
    
    private static SupabaseConfig instance;
    private final String url;
    private final String apiKey;
    
    private SupabaseConfig() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("supabase.properties")) {
            
            if (input == null) {
                throw new RuntimeException("Unable to find supabase.properties");
            }
            
            properties.load(input);
            this.url = properties.getProperty("supabase.url");
            this.apiKey = properties.getProperty("supabase.key");
            
            if (url == null || apiKey == null) {
                throw new RuntimeException("Supabase URL or API key not configured");
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Error loading Supabase configuration", e);
        }
    }
    
    public static synchronized SupabaseConfig getInstance() {
        if (instance == null) {
            instance = new SupabaseConfig();
        }
        return instance;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getRestUrl() {
        return url + "/rest/v1";
    }
}

