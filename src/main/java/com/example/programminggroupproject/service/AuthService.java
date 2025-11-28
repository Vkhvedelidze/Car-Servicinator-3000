package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.User;

public class AuthService {

    public static User authenticate(String username, String password) {

        // Hardcoded test users for now
        if (username.equals("client") && password.equals("client123")) {
            return new User("client", "client", "Client User", "client@example.com");
        }
        if (username.equals("mechanic") && password.equals("mechanic123")) {
            return new User("mechanic", "mechanic", "Mechanic User", "mechanic@example.com");
        }
        if (username.equals("admin") && password.equals("admin123")) {
            return new User("admin", "admin", "Admin User", "admin@example.com");
        }

        return null; // invalid login
    }
}