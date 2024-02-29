package com.cinar.authentication.controller;

import com.cinar.authentication.dto.request.SignInRequest;
import com.cinar.authentication.dto.request.SignUpRequest;
import com.cinar.authentication.dto.response.JwtAuthenticationResponse;
import com.cinar.authentication.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("v1/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {


        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.signUpNewUser(signUpRequest));
    }
    @PostMapping("/signIn")
    public ResponseEntity<JwtAuthenticationResponse> signIn(@RequestBody SignInRequest signInRequest) {
        return ResponseEntity.ok(authService.signIn(signInRequest));
    }
    @PostMapping("/refresh")
    public void refreshToken(HttpServletRequest request,HttpServletResponse response) throws IOException {
        authService.refreshToken(request, response);
    }


}
