package com.example.programminggroupproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a user from Supabase auth.users table.
 * This contains the authentication data returned by Supabase Auth API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthUser {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("aud")
    private String audience;
    
    @JsonProperty("role")
    private String authRole; // This is the auth role (authenticated, anon), not our app role
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("email_confirmed_at")
    private OffsetDateTime emailConfirmedAt;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("confirmed_at")
    private OffsetDateTime confirmedAt;
    
    @JsonProperty("last_sign_in_at")
    private OffsetDateTime lastSignInAt;
    
    @JsonProperty("app_metadata")
    private Map<String, Object> appMetadata;
    
    @JsonProperty("user_metadata")
    private Map<String, Object> userMetadata;
    
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
    
    // Default constructor for Jackson
    public AuthUser() {
    }
    
    /**
     * Get a value from user metadata
     * @param key The metadata key
     * @return The value, or null if not found
     */
    public Object getUserMetadata(String key) {
        if (userMetadata == null) {
            return null;
        }
        return userMetadata.get(key);
    }
    
    /**
     * Get user's full name from metadata
     * @return Full name, or empty string if not found
     */
    @JsonIgnore
    public String getFullName() {
        Object fullName = getUserMetadata("full_name");
        return fullName != null ? fullName.toString() : "";
    }
    
    /**
     * Get user's role from metadata (app role, not auth role)
     * @return Role (client, mechanic, admin), or "client" as default
     */
    @JsonIgnore
    public String getRole() {
        Object role = getUserMetadata("role");
        return role != null ? role.toString() : "client";
    }
    
    /**
     * Get user's shop ID from metadata
     * @return Shop ID, or null if not found
     */
    @JsonIgnore
    public UUID getShopId() {
        Object shopId = getUserMetadata("shop_id");
        if (shopId == null) {
            return null;
        }
        
        try {
            return UUID.fromString(shopId.toString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getAudience() {
        return audience;
    }
    
    public void setAudience(String audience) {
        this.audience = audience;
    }
    
    public String getAuthRole() {
        return authRole;
    }
    
    public void setAuthRole(String authRole) {
        this.authRole = authRole;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public OffsetDateTime getEmailConfirmedAt() {
        return emailConfirmedAt;
    }
    
    public void setEmailConfirmedAt(OffsetDateTime emailConfirmedAt) {
        this.emailConfirmedAt = emailConfirmedAt;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public OffsetDateTime getConfirmedAt() {
        return confirmedAt;
    }
    
    public void setConfirmedAt(OffsetDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
    
    public OffsetDateTime getLastSignInAt() {
        return lastSignInAt;
    }
    
    public void setLastSignInAt(OffsetDateTime lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }
    
    public Map<String, Object> getAppMetadata() {
        return appMetadata;
    }
    
    public void setAppMetadata(Map<String, Object> appMetadata) {
        this.appMetadata = appMetadata;
    }
    
    public Map<String, Object> getUserMetadata() {
        return userMetadata;
    }
    
    public void setUserMetadata(Map<String, Object> userMetadata) {
        this.userMetadata = userMetadata;
    }
    
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "AuthUser{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", authRole='" + authRole + '\'' +
                ", emailConfirmedAt=" + emailConfirmedAt +
                ", lastSignInAt=" + lastSignInAt +
                ", userMetadata=" + userMetadata +
                '}';
    }
}

