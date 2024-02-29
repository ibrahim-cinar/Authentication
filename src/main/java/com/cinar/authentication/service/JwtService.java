package com.cinar.authentication.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.key}")
    private String SECRET;
    @Value("${jwt.expiration}")
    private long EXPIRATION;
    @Value("${jwt.refresh.token.expiration}")
    private long REFRESH_EXPIRATION;


    public String generateToken( UserDetails userDetails){
        return buildToken(new HashMap<>(),userDetails,EXPIRATION);

    }
    private <T> T   extractClaim(String token, Function<Claims, T> claimsResolver){

        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private String buildToken(Map<String,Object> extraClaims,UserDetails userDetails,long expiration){
        Map<String,Object> claims = new HashMap<>();
        claims.put("role",userDetails.getAuthorities());
        claims.put("username",userDetails.getUsername());
        claims.putAll(extraClaims);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();

    }
    public String generateRefreshToken( UserDetails userDetails){
        return buildToken(new HashMap<>(),userDetails,REFRESH_EXPIRATION);

    }

    public String extractUser(String token){
        return extractClaim(token, Claims::getSubject);
    }


    private Date extractExpiration(String token){
    return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }


    public Boolean isValidateToken(String token, UserDetails userDetails){
        String username = extractUser(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private Key getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
