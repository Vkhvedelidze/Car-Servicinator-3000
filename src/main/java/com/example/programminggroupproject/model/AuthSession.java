package com.example.programminggroupproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an authentication session with JWT tokens.
 * Contains access token, refresh token, expiration time, and user information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthSession {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("expires_in")
    private Long expiresIn; // Seconds until expiration
    
    @JsonProperty("expires_at")
    private Long expiresAt; // Unix timestamp of expiration
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("user")
    private AuthUser authUser;
    
    // Cached User object from public.users (not from JSON)
    private User user;
    
    // Default constructor for Jackson
    public AuthSession() {
    }
    
    // Constructor for creating sessions
    public AuthSession(String accessToken, String refreshToken, Long expiresIn, AuthUser authUser) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.authUser = authUser;
        
        // Calculate expiresAt if expiresIn is provided
        if (expiresIn != null) {
            this.expiresAt = System.currentTimeMillis() / 1000 + expiresIn;
        }
    }
    
    /**
     * Check if the access token is expired or about to expire
     * @return true if token is expired or will expire in the next 60 seconds
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false; // Can't determine, assume valid
        }
        
        long currentTime = System.currentTimeMillis() / 1000;
        // Consider expired if less than 60 seconds remaining
        return currentTime >= (expiresAt - 60);
    }
    
    /**
     * Check if the session is valid (has tokens and not expired)
     * @return true if session is valid
     */
    public boolean isValid() {
        return accessToken != null && !accessToken.isEmpty() && !isExpired();
    }
    
    /**
     * Get time remaining until expiration in seconds
     * @return seconds until expiration, or 0 if expired
     */
    public long getTimeUntilExpiration() {
        if (expiresAt == null) {
            return Long.MAX_VALUE;
        }
        
        long currentTime = System.currentTimeMillis() / 1000;
        long remaining = expiresAt - currentTime;
        return Math.max(0, remaining);
    }
    
    // Getters and Setters
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
        // Recalculate expiresAt
        if (expiresIn != null) {
            this.expiresAt = System.currentTimeMillis() / 1000 + expiresIn;
        }
    }
    
    public Long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public AuthUser getAuthUser() {
        return authUser;
    }
    
    public void setAuthUser(AuthUser authUser) {
        this.authUser = authUser;
    }
    
    @JsonIgnore
    public User getUser() {
        return user;
    }
    
    @JsonIgnore
    public void setUser(User user) {
        this.user = user;
    }
    
    @Override
    public String toString() {
        return "AuthSession{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", expiresAt=" + expiresAt +
                ", isExpired=" + isExpired() +
                ", authUser=" + authUser +
                ", user=" + user +
                '}';
    }
}

