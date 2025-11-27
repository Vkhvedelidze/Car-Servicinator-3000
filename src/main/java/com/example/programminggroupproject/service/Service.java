package com.example.programminggroupproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.UUID;

public interface Service<T> {

    String SUPABASE_URL = "https://your-project.supabase.co";
    String SUPABASE_KEY = "your-anon-key";

    T get(UUID id);
    ArrayList<T> getAll();
    T put(UUID id, T object);
    T post(T object);
    void delete(UUID id);
    void deleteAll();
}
