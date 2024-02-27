package com.cinar.authentication.controller;

import com.cinar.authentication.dto.UserDto;
import com.cinar.authentication.dto.request.RefreshTokenRequest;
import com.cinar.authentication.dto.request.SignInRequest;
import com.cinar.authentication.dto.request.SignUpRequest;
import com.cinar.authentication.dto.response.JwtAuthenticationResponse;
import com.cinar.authentication.model.User;
import com.cinar.authentication.service.AuthService;
import com.cinar.authentication.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {


        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signUp(@RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.signUpNewUser(signUpRequest));
    }
    @PostMapping("/signIn")
    public ResponseEntity<JwtAuthenticationResponse> signIn(@RequestBody SignInRequest signInRequest) {
        return ResponseEntity.ok(authService.signIn(signInRequest));
    }
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }


}
