package com.cinar.authentication.service;

import com.cinar.authentication.dto.request.CreateUserRequest;
import com.cinar.authentication.dto.request.RefreshTokenRequest;
import com.cinar.authentication.dto.request.SignInRequest;
import com.cinar.authentication.dto.request.SignUpRequest;
import com.cinar.authentication.dto.response.JwtAuthenticationResponse;
import com.cinar.authentication.exceptions.EmailAlreadyExistException;
import com.cinar.authentication.exceptions.InvalidInputException;
import com.cinar.authentication.exceptions.UserNotFoundException;
import com.cinar.authentication.model.Role;
import com.cinar.authentication.model.User;
import com.cinar.authentication.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }


        public static boolean patternMatches(String emailAddress, String regexPattern) {
            return Pattern.compile(regexPattern)
                    .matcher(emailAddress)
                    .matches();

    }
    protected boolean isEmailUnique(String email) {
        Optional<User> existingUserEmail = userRepository.findUserByEmail(email);
        return existingUserEmail.isEmpty();
    }
    private static boolean isInputValid(SignUpRequest request) {
        return
                request.getFirstName() != null &&
                        request.getLastName() != null &&
                        request.getEmail() != null &&
                        request.getPassword() != null;
    }
    public User signUp(SignUpRequest signUpRequest) {
        return new User(
                signUpRequest.getFirstName(), signUpRequest.getLastName(),
                signUpRequest.getEmail(),
                bCryptPasswordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getPhoneNumber(),
                List.of(Role.valueOf("ROLE_USER")));
    }
    public User signUpFromRequest(SignUpRequest signUpRequest) {
        if (!isInputValid(signUpRequest)) {
            throw new InvalidInputException("Invalid input");
        }
        if (!isEmailUnique(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistException("Email already exists");
        }

        if (!patternMatches(signUpRequest.getEmail(), "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            throw new InvalidInputException("Email is not valid");
        }
        if (!patternMatches(signUpRequest.getPhoneNumber(), "\\d{10}|(?:\\d{3}-){2}\\d{4}|\\(\\d{3}\\)\\d{3}-?\\d{4}")) {
            throw new InvalidInputException("Phone number is not valid");
        }
        return signUp(signUpRequest);
    }

    public User signUpNewUser(SignUpRequest signUpRequest) {
        User user = signUpFromRequest(signUpRequest);
        return userRepository.save(user);
    }
    public JwtAuthenticationResponse signIn(SignInRequest signInRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getEmail(),
                signInRequest.getPassword()));

        var user = userRepository.findUserByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with this email"+signInRequest.getEmail()));
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setAccessToken(jwtToken);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);
        return jwtAuthenticationResponse;
    }
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String userEmail = jwtService.extractUser(refreshTokenRequest.getToken());
        var user = userRepository.findUserByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("User not found with this email"+userEmail));
        if(jwtService.validateToken(refreshTokenRequest.getToken(),user)){
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setAccessToken(jwtToken);
            jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());
            return jwtAuthenticationResponse;
        }
        return null;

    }


}
