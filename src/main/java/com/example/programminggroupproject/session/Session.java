package com.example.programminggroupproject.session;

import com.example.programminggroupproject.model.AuthSession;
import com.example.programminggroupproject.model.User;

/**
 * Session management for the application.
 * Stores the current authenticated user and their auth session with JWT tokens.
 */
public class Session {

    private static User currentUser;
    private static AuthSession currentSession;

    /**
     * Set the current authenticated user
     * @param user The user object
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Get the current authenticated user
     * @return The current user, or null if not authenticated
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Set the current authentication session
     * @param session The AuthSession containing tokens and user data
     */
    public static void setCurrentSession(AuthSession session) {
        currentSession = session;
        
        // Also set the user from the session for convenience
        if (session != null && session.getUser() != null) {
            currentUser = session.getUser();
        }
    }

    /**
     * Get the current authentication session
     * @return The current AuthSession, or null if not authenticated
     */
    public static AuthSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Check if there is a valid session
     * @return true if session exists and is valid
     */
    public static boolean hasValidSession() {
        return currentSession != null && currentSession.isValid();
    }

    /**
     * Get the current access token
     * @return The access token, or null if no session
     */
    public static String getAccessToken() {
        return currentSession != null ? currentSession.getAccessToken() : null;
    }

    /**
     * Clear the current session and user
     * Called on logout or when session expires
     */
    public static void clear() {
        currentUser = null;
        currentSession = null;
    }
}