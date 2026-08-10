package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.UserLoginDto;
import com.example.cloudstorage.dto.UserRegisterDto;
import com.example.cloudstorage.exception.UserAlreadyExistsException;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public void register(UserRegisterDto userRegisterDto) {
        if (userRepository.findByUsernameIgnoreCase(userRegisterDto.username()).isPresent()) {
            throw new UserAlreadyExistsException("Username is already taken");
        }
        String hashedPassword = passwordEncoder.encode(userRegisterDto.password());
        User userToSave = new User(userRegisterDto.username(), hashedPassword);
        userRepository.save(userToSave);
        authenticate(new UserLoginDto(userRegisterDto.username(), userRegisterDto.password()));
    }

    public void authenticate(UserLoginDto userLoginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.username(), userLoginDto.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}