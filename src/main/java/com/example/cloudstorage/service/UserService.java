package com.example.cloudstorage.service;

import com.example.cloudstorage.model.User;

import java.util.Optional;

public interface UserService {

    User createUser(String username, String rawPassword);

    Optional<User> findByUsername(String username);
}