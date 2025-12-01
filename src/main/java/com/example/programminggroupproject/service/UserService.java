package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.User;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing User entities with Supabase backend.
 * Provides user-specific operations in addition to standard CRUD.
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
     * Authenticate a user with username and password
     * @param username The username
     * @param password The password
     * @return Optional containing the User if authentication succeeds
     */
    public Optional<User> authenticate(String username, String password) {
        try {
            // Note: In production, you should use Supabase Auth API instead
            // This is simplified for demonstration
            List<User> users = filter("username", "eq", username);
            
            return users.stream()
                    .filter(user -> user.getPassword().equals(password))
                    .findFirst();
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }
    
    /**
     * Find a user by username
     * @param username The username to search for
     * @return Optional containing the User if found
     */
    public Optional<User> findByUsername(String username) {
        return findOneBy("username", username);
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
     * Check if a username already exists
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return existsBy("username", username);
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
     * @throws IllegalArgumentException if username or email already exists
     */
    public User registerUser(User user) {
        if (usernameExists(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
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
        return searchMultiple(searchTerm, "full_name", "email", "username");
    }
}
