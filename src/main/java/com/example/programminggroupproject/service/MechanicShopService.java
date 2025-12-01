package com.example.programminggroupproject.service;

import com.example.programminggroupproject.model.MechanicShop;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Service for managing MechanicShop entities with Supabase backend.
 * Provides mechanic shop-specific operations in addition to standard CRUD.
 */
public class MechanicShopService extends BaseSupabaseService<MechanicShop> {
    
    private static MechanicShopService instance;
    
    private MechanicShopService() {
        super("mechanic_shops", MechanicShop.class, new TypeReference<List<MechanicShop>>() {});
    }
    
    /**
     * Get singleton instance of MechanicShopService
     */
    public static synchronized MechanicShopService getInstance() {
        if (instance == null) {
            instance = new MechanicShopService();
        }
        return instance;
    }
    
    // ==================== MECHANIC SHOP-SPECIFIC OPERATIONS ====================
    
    /**
     * Search shops by name
     * @param name The shop name to search for
     * @return List of matching shops
     */
    public List<MechanicShop> searchByName(String name) {
        return search("name", name);
    }
    
    /**
     * Get shops by city
     * @param city The city name
     * @return List of shops in the specified city
     */
    public List<MechanicShop> getByCity(String city) {
        return findBy("city", city);
    }
    
    /**
     * Search shops by name, city, or address
     * @param searchTerm The search term
     * @return List of matching shops
     */
    public List<MechanicShop> searchShops(String searchTerm) {
        return searchMultiple(searchTerm, "name", "city", "address");
    }
    
    /**
     * Get all shops ordered by name
     * @return List of shops ordered alphabetically by name
     */
    public List<MechanicShop> getAllOrderedByName() {
        return getAllOrdered("name", true);
    }
    
    /**
     * Get all shops ordered by city
     * @return List of shops ordered alphabetically by city
     */
    public List<MechanicShop> getAllOrderedByCity() {
        return getAllOrdered("city", true);
    }
}

