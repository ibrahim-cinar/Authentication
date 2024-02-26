package com.cinar.authentication.controller;

import com.cinar.authentication.dto.request.AuthRequest;
import com.cinar.authentication.service.JwtService;
import com.cinar.authentication.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/api/auth")
@Slf4j
public class AuthController {

    private final UserService service;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;


    public AuthController(UserService service, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.service = service;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }


    @PostMapping("/generateToken")
    public String generateToken(@RequestBody AuthRequest authRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(),authRequest.getPassword()));
        if(authentication.isAuthenticated()){
            return jwtService.generateToken(authRequest.getUsername());
        }
        throw new UsernameNotFoundException("Invalid username {}" +authRequest.getUsername());
    }
}
