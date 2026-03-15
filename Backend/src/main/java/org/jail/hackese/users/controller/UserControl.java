package org.jail.hackese.users.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jail.hackese.security.service.DetailsService;
import org.jail.hackese.security.service.JWTService;
import org.jail.hackese.users.dto.RequestDTO;
import org.jail.hackese.users.dto.ResponseDTO;
import org.jail.hackese.users.entity.User;
import org.jail.hackese.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
public class UserControl {

    @Autowired
    private UserService userService;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private DetailsService detailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RequestDTO requestDTO){
        User register = userService.registerUser(requestDTO);
        if(register==null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists with given email");
        }
        return ResponseEntity.ok().body("User Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RequestDTO requestDTO, HttpServletResponse response){
        ResponseDTO loggedin = userService.loginUser(requestDTO);
        if(loggedin==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        response.addHeader(HttpHeaders.SET_COOKIE,jwtService.generateRefreshCookie(loggedin).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,jwtService.generateAccessCookie(loggedin).toString());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();

        if(cookies==null){return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();}
        Cookie refreshCookie = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("REFRESH_COOKIE"))
                .findFirst().orElse(null);

        if(refreshCookie==null){return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();}
        String refreshToken = refreshCookie.getValue();

        String username = jwtService.extractUsername(refreshToken);
        UserDetails details =  detailsService.loadUserByUsername(username);

        if (!jwtService.validate(refreshToken, details)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        response.addHeader(HttpHeaders.SET_COOKIE, jwtService
                .generateAccessCookie(new ResponseDTO(Long.parseLong(details.getUsername()))).toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        if (session!=null){
            session.invalidate();
        }
        Cookie refreshCookie = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("REFRESH_COOKIE"))
                .findFirst().orElse(null);
        Cookie accessCookie = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("ACCESS_COOKIE"))
                .findFirst().orElse(null);
        if (refreshCookie != null) {
            refreshCookie.setMaxAge(0);
            refreshCookie.setValue("");
            response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }
        if (accessCookie != null) {
            accessCookie.setMaxAge(0);
            accessCookie.setValue("");
            response.setHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        }
        return ResponseEntity.noContent().build();
    }
}
