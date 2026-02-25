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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JWTService {

    private static String secretkey;
    private static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @PostConstruct
    public void init() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = generator.generateKey();
            secretkey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
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

    public boolean validate(String token, UserDetails user){
        return (user.getUsername().equals(extractUsername(token)));
    }

    public String generateToken(ResponseDTO dto){
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(dto.userid().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                // need to add expiry. after discussion
                .and()
                .signWith(retriveKey())
                .compact();
    }

}