package com.example.programminggroupproject.service;

import com.example.programminggroupproject.client.SupabaseAuthClient;
import com.example.programminggroupproject.model.AuthResponse;
import com.example.programminggroupproject.model.AuthSession;
import com.example.programminggroupproject.model.AuthUser;
import com.example.programminggroupproject.model.User;
import com.example.programminggroupproject.session.Session;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling Supabase Authentication.
 * Manages user sign up, sign in, sign out, and session management.
 */
public class SupabaseAuthService {
    
    private static SupabaseAuthService instance;
    private final SupabaseAuthClient authClient;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    
    private SupabaseAuthService() {
        this.authClient = SupabaseAuthClient.getInstance();
        this.userService = UserService.getInstance();
        this.objectMapper = authClient.getObjectMapper();
    }
    
    /**
     * Get singleton instance of SupabaseAuthService
     */
    public static synchronized SupabaseAuthService getInstance() {
        if (instance == null) {
            instance = new SupabaseAuthService();
        }
        return instance;
    }
    
    /**
     * Sign up a new user with Supabase Auth
     * 
     * @param email User's email
     * @param password User's password
     * @param fullName User's full name
     * @param role User's role (client, mechanic, admin)
     * @param shopId Optional shop ID (can be null)
     * @return User object with metadata, or null if signup fails
     */
    public User signUp(String email, String password, String fullName, String role, UUID shopId) {
        try {
            // Prepare user metadata to be stored in auth.users
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("full_name", fullName);
            metadata.put("role", role);
            if (shopId != null) {
                metadata.put("shop_id", shopId.toString());
            }
            
            // Call Supabase Auth signup endpoint
            String responseJson = authClient.signUp(email, password, metadata);
            
            // Parse the auth response
            AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);
            
            if (!authResponse.hasValidSession()) {
                System.err.println("Signup failed: No valid session in response");
                return null;
            }
            
            // Create AuthSession from response
            AuthSession authSession = authResponse.toAuthSession();
            AuthUser authUser = authResponse.getUser();
            
            if (authUser == null) {
                System.err.println("Signup failed: No user data in response");
                return null;
            }
            
            // Wait a moment for the database trigger to create the public.users entry
            Thread.sleep(500);
            
            // Fetch the user metadata from public.users table using auth_user_id
            Optional<User> userOptional = userService.findBy("auth_user_id", authUser.getId())
                    .stream().findFirst();
            
            if (userOptional.isEmpty()) {
                System.err.println("Warning: User created in auth but not found in public.users. Trigger may have failed.");
                // Create the user manually as fallback
                User newUser = new User(authUser.getId(), email, fullName, role, shopId);
                User createdUser = userService.create(newUser);
                authSession.setUser(createdUser);
            } else {
                authSession.setUser(userOptional.get());
            }
            
            // Store session
            Session.setCurrentSession(authSession);
            Session.setCurrentUser(authSession.getUser());
            
            return authSession.getUser();
            
        } catch (IOException e) {
            System.err.println("Signup failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Signup interrupted: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error during signup: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Sign in an existing user with email and password
     * 
     * @param email User's email
     * @param password User's password
     * @return User object with metadata, or null if signin fails
     */
    public User signIn(String email, String password) {
        try {
            // Call Supabase Auth signin endpoint
            String responseJson = authClient.signIn(email, password);
            
            // Parse the auth response
            AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);
            
            if (!authResponse.hasValidSession()) {
                System.err.println("Signin failed: No valid session in response");
                return null;
            }
            
            // Create AuthSession from response
            AuthSession authSession = authResponse.toAuthSession();
            AuthUser authUser = authResponse.getUser();
            
            if (authUser == null) {
                System.err.println("Signin failed: No user data in response");
                return null;
            }
            
            // Fetch the user metadata from public.users table using auth_user_id
            Optional<User> userOptional = userService.findBy("auth_user_id", authUser.getId())
                    .stream().findFirst();
            
            if (userOptional.isEmpty()) {
                System.err.println("Signin failed: User not found in public.users table");
                return null;
            }
            
            // Set user in session
            authSession.setUser(userOptional.get());
            
            // Store session
            Session.setCurrentSession(authSession);
            Session.setCurrentUser(authSession.getUser());
            
            return authSession.getUser();
            
        } catch (IOException e) {
            System.err.println("Signin failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error during signin: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Sign out the current user
     * Clears the session and invalidates tokens
     */
    public void signOut() {
        try {
            AuthSession currentSession = Session.getCurrentSession();
            
            if (currentSession != null && currentSession.getAccessToken() != null) {
                // Call Supabase Auth logout endpoint
                authClient.signOut(currentSession.getAccessToken());
            }
            
        } catch (IOException e) {
            System.err.println("Error during signout: " + e.getMessage());
            // Continue to clear local session even if API call fails
        } finally {
            // Always clear local session
            Session.clear();
        }
    }
    
    /**
     * Refresh the current session using the refresh token
     * 
     * @return true if refresh was successful, false otherwise
     */
    public boolean refreshSession() {
        try {
            AuthSession currentSession = Session.getCurrentSession();
            
            if (currentSession == null || currentSession.getRefreshToken() == null) {
                System.err.println("Cannot refresh: No current session or refresh token");
                return false;
            }
            
            // Call Supabase Auth refresh endpoint
            String responseJson = authClient.refreshSession(currentSession.getRefreshToken());
            
            // Parse the auth response
            AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);
            
            if (!authResponse.hasValidSession()) {
                System.err.println("Refresh failed: No valid session in response");
                return false;
            }
            
            // Create new AuthSession from response
            AuthSession newAuthSession = authResponse.toAuthSession();
            
            // Keep the existing User object
            newAuthSession.setUser(currentSession.getUser());
            
            // Update session
            Session.setCurrentSession(newAuthSession);
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Session refresh failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error during session refresh: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get the current session, automatically refreshing if expired
     * 
     * @return Current AuthSession, or null if no valid session
     */
    public AuthSession getCurrentSession() {
        AuthSession currentSession = Session.getCurrentSession();
        
        if (currentSession == null) {
            return null;
        }
        
        // Check if session is expired or about to expire
        if (currentSession.isExpired()) {
            System.out.println("Session expired or expiring soon, attempting to refresh...");
            
            // Try to refresh
            boolean refreshed = refreshSession();
            
            if (!refreshed) {
                System.err.println("Failed to refresh session, clearing session");
                Session.clear();
                return null;
            }
            
            // Return the refreshed session
            return Session.getCurrentSession();
        }
        
        // Session is still valid
        return currentSession;
    }
    
    /**
     * Check if there is a valid authenticated session
     * 
     * @return true if user is authenticated with valid session
     */
    public boolean isAuthenticated() {
        AuthSession session = getCurrentSession();
        return session != null && session.isValid();
    }
    
    /**
     * Get the current authenticated user
     * 
     * @return User object, or null if not authenticated
     */
    public User getCurrentUser() {
        if (!isAuthenticated()) {
            return null;
        }
        
        return Session.getCurrentUser();
    }
}

