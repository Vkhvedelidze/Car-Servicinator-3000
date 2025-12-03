package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.User;
import java.util.UUID;

/**
 * Authentication service using Supabase Auth.
 * This is a facade that delegates to SupabaseAuthService.
 * Maintains backward compatibility with existing controllers.
 */
public class AuthService {

    private static final SupabaseAuthService authService = SupabaseAuthService.getInstance();

    /**
     * Authenticate a user with email and password using Supabase Auth
     * @param email The user's email
     * @param password The password
     * @return User object if authentication succeeds, null otherwise
     */
    public static User authenticate(String email, String password) {
        return authService.signIn(email, password);
    }

    /**
     * Register a new user with Supabase Auth
     * @param email The user's email
     * @param password The password (will be securely hashed by Supabase)
     * @param fullName The user's full name
     * @param role The user's role (client, mechanic, admin)
     * @param shopId Optional shop ID (null if not associated with a shop)
     * @return true if registration succeeds, false otherwise
     */
    public static boolean register(String email, String password, String fullName, String role, UUID shopId) {
        // Basic validation
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        try {
            User user = authService.signUp(email, password, fullName, role, shopId);
            return user != null;
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Register a new user without shop association
     * @param email The user's email
     * @param password The password
     * @param fullName The user's full name
     * @param role The user's role
     * @return true if registration succeeds, false otherwise
     */
    public static boolean register(String email, String password, String fullName, String role) {
        return register(email, password, fullName, role, null);
    }
    
    /**
     * Sign out the current user
     */
    public static void signOut() {
        authService.signOut();
    }
    
    /**
     * Check if user is authenticated
     * @return true if user has valid session
     */
    public static boolean isAuthenticated() {
        return authService.isAuthenticated();
    }
    
    /**
     * Get the current authenticated user
     * @return User object, or null if not authenticated
     */
    public static User getCurrentUser() {
        return authService.getCurrentUser();
    }
}