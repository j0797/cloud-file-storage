package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.UserLoginDto;
import com.example.cloudstorage.dto.UserRegisterDto;

public interface AuthService {

    void register(UserRegisterDto userRegisterDto);

    void authenticate(UserLoginDto userLoginDto);
}