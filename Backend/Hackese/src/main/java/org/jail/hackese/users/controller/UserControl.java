package org.jail.hackese.users.controller;

import org.jail.hackese.security.service.JWTService;
import org.jail.hackese.users.dto.RequestDTO;
import org.jail.hackese.users.dto.ResponseDTO;
import org.jail.hackese.users.entity.User;
import org.jail.hackese.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserControl {

    @Autowired
    private UserService userService;
    @Autowired
    private JWTService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        User register = userService.registerUser(user);
        if(register==null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists with given email");
        }
        return ResponseEntity.ok().body("User Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RequestDTO requestDTO){
        ResponseDTO loggedin = userService.loginUser(requestDTO);
        if(loggedin==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
        return ResponseEntity.ok()
                .header("Authorization","Bearer "+jwtService.generateToken(loggedin))
                .build();
    }
}
