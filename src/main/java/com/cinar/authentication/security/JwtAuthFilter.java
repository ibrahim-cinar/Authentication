package com.cinar.authentication.security;

import com.cinar.authentication.config.UserContextHolder;
import com.cinar.authentication.repository.TokenRepository;
import com.cinar.authentication.service.JwtService;
import com.cinar.authentication.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final TokenRepository tokenRepository;

    public JwtAuthFilter(JwtService jwtService, UserService userService, TokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token ;
        String userEmail ;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;

        }
        token = authHeader.substring(7);
        userEmail = jwtService.extractUser(token);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = userService.loadUserByUsername(userEmail);
            UserContextHolder.setUser(user);
            log.info("setting security context for user " + UserContextHolder.getUser().getUsername());
            log.info("user loaded " + user);
            var isTokenValid = tokenRepository.findByToken(token).map(token1 -> !token1.isExpired()&& !token1.isRevoked()).orElse(false);
            if (jwtService.isValidateToken(token, user) && isTokenValid) {
                log.info("token validated " + token);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }
        filterChain.doFilter(request, response);
    }
}

