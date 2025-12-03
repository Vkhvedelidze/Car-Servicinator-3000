package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.User;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing User entities with Supabase backend.
 * Provides user-specific operations in addition to standard CRUD.
 * Schema: id (UUID), email, password_hash, full_name, role, shop_id, created_at
 */
public class UserService extends BaseSupabaseService<User> {
    
    private static UserService instance;
    
    private UserService() {
        super("users", User.class, new TypeReference<List<User>>() {});
    }
    
    /**
     * Get singleton instance of UserService
     */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    // ==================== USER-SPECIFIC OPERATIONS ====================
    
    /**
     * Authenticate a user with email and password
     * @deprecated Use SupabaseAuthService.signIn() instead for proper authentication
     * @param email The user's email
     * @param password The password
     * @return Optional containing the User if authentication succeeds
     */
    @Deprecated
    public Optional<User> authenticate(String email, String password) {
        // This method is deprecated - authentication is now handled by Supabase Auth
        // Use SupabaseAuthService.signIn() instead
        System.err.println("Warning: UserService.authenticate() is deprecated. Use SupabaseAuthService.signIn() instead.");
        return Optional.empty();
    }
    
    /**
     * Find a user by email
     * @param email The email to search for
     * @return Optional containing the User if found
     */
    public Optional<User> findByEmail(String email) {
        return findOneBy("email", email);
    }
    
    /**
     * Get all users by role
     * @param role The role to filter by (e.g., "client", "mechanic", "admin")
     * @return List of users with the specified role
     */
    public List<User> getUsersByRole(String role) {
        return findBy("role", role);
    }
    
    /**
     * Get all users by shop ID
     * @param shopId The shop ID to filter by
     * @return List of users associated with the shop
     */
    public List<User> getUsersByShopId(Long shopId) {
        return findBy("shop_id", shopId);
    }
    
    /**
     * Check if an email already exists
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        return existsBy("email", email);
    }
    
    /**
     * Register a new user
     * @param user The user to register
     * @return The created user with generated ID
     * @throws IllegalArgumentException if email already exists
     */
    public User registerUser(User user) {
        if (emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        return create(user);
    }
    
    /**
     * Search users by name or email
     * @param searchTerm The search term
     * @return List of matching users
     */
    public List<User> searchUsers(String searchTerm) {
        return searchMultiple(searchTerm, "full_name", "email");
    }
    
    /**
     * Get user by ID
     * @param id The user ID (UUID)
     * @return Optional containing the User if found
     */
    public Optional<User> getUserById(UUID id) {
        return get(id);
    }
}
