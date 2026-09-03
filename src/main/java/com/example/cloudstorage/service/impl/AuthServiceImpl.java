package com.example.cloudstorage.service.impl;

import com.example.cloudstorage.dto.UserLoginDto;
import com.example.cloudstorage.dto.UserRegisterDto;
import com.example.cloudstorage.service.AuthService;
import com.example.cloudstorage.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final HttpServletRequest request;

    public AuthServiceImpl(UserService userService, AuthenticationManager authenticationManager, HttpServletRequest request) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.request = request;
    }

    @Override
    public void register(UserRegisterDto userRegisterDto) {
        userService.createUser(userRegisterDto.username(), userRegisterDto.password());
        authenticate(new UserLoginDto(userRegisterDto.username(), userRegisterDto.password()));
    }

    @Override
    public void authenticate(UserLoginDto userLoginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.username(), userLoginDto.password())
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}