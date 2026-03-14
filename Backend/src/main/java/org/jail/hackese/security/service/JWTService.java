package org.jail.hackese.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.jail.hackese.users.dto.RequestDTO;
import org.jail.hackese.users.dto.ResponseDTO;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JWTService {

    private static String secretkey = null;
    private static BCryptPasswordEncoder encoder = null;

    @PostConstruct
    public void init() {
        try {
            encoder = new BCryptPasswordEncoder(12);
            KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = generator.generateKey();
            secretkey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Error: "+e);
        }
    }

    public String encodePassword(String password){
        return encoder.encode(password);
    }

    public BCryptPasswordEncoder getEncoder() {
        return encoder;
    }

    private SecretKey retriveKey(){
        byte[] encodedkey = Decoders.BASE64.decode(secretkey);
        return Keys.hmacShaKeyFor(encodedkey);
    }

    public boolean matchPassword(String rawPassword, String encodedPassword){
        return encoder.matches(rawPassword,encodedPassword);
    }

    public Claims extractAll(String token){
        return Jwts.parser()
                .verifyWith(retriveKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims,T> resolver){
        Claims claims = extractAll(token);
        return resolver.apply(claims);
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public boolean expired(String token){
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean validate(String token, UserDetails user){
        return user.getUsername().equals(extractUsername(token)) && !expired(token);
    }

    private String generateAccessToken(Long userid){
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(userid.toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 3600*1000L))
                .and()
                .signWith(retriveKey())
                .compact();
    }

    public ResponseCookie generateAccessCookie(ResponseDTO dto){
        return ResponseCookie.from("ACCESS_COOKIE",generateAccessToken(dto.userid()))
                .maxAge(3600)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .build();
    }

    private String generateRefreshToken(Long userid){
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(userid.toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration( new Date(System.currentTimeMillis() + 1000*60*60*24*7L))
                .and()
                .signWith(retriveKey())
                .compact();
    }

    public ResponseCookie generateRefreshCookie(ResponseDTO dto){
        return ResponseCookie.from("REFRESH_COOKIE", generateRefreshToken(dto.userid()))
                .maxAge(60*60*24*7L)
                .sameSite("Strict")
                .path("/refresh")
                .httpOnly(true)
                .build();
    }

}