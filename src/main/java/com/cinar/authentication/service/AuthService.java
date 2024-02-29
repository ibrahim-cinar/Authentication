package com.cinar.authentication.service;

import com.cinar.authentication.dto.request.SignInRequest;
import com.cinar.authentication.dto.request.SignUpRequest;
import com.cinar.authentication.dto.response.JwtAuthenticationResponse;
import com.cinar.authentication.exceptions.EmailAlreadyExistException;
import com.cinar.authentication.exceptions.InvalidInputException;
import com.cinar.authentication.exceptions.UserNotFoundException;
import com.cinar.authentication.model.Role;
import com.cinar.authentication.model.Token;
import com.cinar.authentication.model.TokenType;
import com.cinar.authentication.model.User;
import com.cinar.authentication.repository.TokenRepository;
import com.cinar.authentication.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
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
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
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

    public JwtAuthenticationResponse signUpNewUser(SignUpRequest signUpRequest) {
        User user = signUpFromRequest(signUpRequest);
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        savedUserToken(savedUser, jwtToken);

        return JwtAuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
    }

    private void  savedUserToken(User user, String jwtToken) {
        var token = Token.builder().user(user).token(jwtToken).tokenType(TokenType.BEARER).revoked(false).expired(false).build();
        tokenRepository.save(token);}
    private void  revokeAllUserTokens(User user) {
        var validUserToken = tokenRepository.findAllValidTokensByUser(user.getId());
        if(validUserToken.isEmpty())
            return;
        validUserToken.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserToken);

    }

    public JwtAuthenticationResponse signIn(SignInRequest signInRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getEmail(),
                signInRequest.getPassword()));

        var user = userRepository.findUserByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with this email"+signInRequest.getEmail()));
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        savedUserToken(user, jwtToken);
        return JwtAuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
    }
    public void refreshToken(HttpServletRequest request,HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken ;
        String userEmail ;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;

        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUser(refreshToken);

        if (userEmail != null ) {
            var user = this.userRepository.findUserByEmail(userEmail).orElseThrow();
            if (jwtService.isValidateToken(refreshToken, user)) {
                var accessToken =jwtService.generateToken(user);
                revokeAllUserTokens(user);
                savedUserToken(user, accessToken);
                var authResponse = JwtAuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
                new ObjectMapper().writeValue(response.getOutputStream(),authResponse);
            }
        }

    }


}
