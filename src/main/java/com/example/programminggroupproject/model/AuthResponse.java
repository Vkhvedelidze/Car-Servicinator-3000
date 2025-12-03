package com.example.programminggroupproject.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response from Supabase Auth API endpoints.
 * Contains session information (tokens) and user data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("expires_at")
    private Long expiresAt;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("user")
    private AuthUser user;
    
    @JsonProperty("session")
    private AuthSession session;
    
    // Default constructor for Jackson
    public AuthResponse() {
    }
    
    /**
     * Build an AuthSession from this response
     * @return AuthSession object with tokens and user data
     */
    public AuthSession toAuthSession() {
        // If session is already populated, use it
        if (session != null) {
            return session;
        }
        
        // Otherwise, build session from top-level fields
        AuthSession authSession = new AuthSession();
        authSession.setAccessToken(accessToken);
        authSession.setRefreshToken(refreshToken);
        authSession.setExpiresIn(expiresIn);
        authSession.setExpiresAt(expiresAt);
        authSession.setTokenType(tokenType);
        authSession.setAuthUser(user);
        
        return authSession;
    }
    
    /**
     * Check if the response contains valid session data
     * @return true if access token is present
     */
    public boolean hasValidSession() {
        return (accessToken != null && !accessToken.isEmpty()) || 
               (session != null && session.getAccessToken() != null);
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
    
    public AuthUser getUser() {
        return user;
    }
    
    public void setUser(AuthUser user) {
        this.user = user;
    }
    
    public AuthSession getSession() {
        return session;
    }
    
    public void setSession(AuthSession session) {
        this.session = session;
    }
    
    @Override
    public String toString() {
        return "AuthResponse{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", hasSession=" + hasValidSession() +
                ", user=" + user +
                '}';
    }
}

