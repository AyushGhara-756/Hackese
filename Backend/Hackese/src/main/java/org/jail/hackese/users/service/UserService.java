package org.jail.hackese.users.service;

import org.jail.hackese.security.service.JWTService;
import org.jail.hackese.users.dto.RequestDTO;
import org.jail.hackese.users.dto.ResponseDTO;
import org.jail.hackese.users.entity.User;
import org.jail.hackese.users.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;
    @Autowired
    private JWTService jwtservice;

    public User findByUsername(String username){
        return repo.findById(Long.parseLong(username)).get();
    }

    public User registerUser(User user){
        if (!repo.existsByEmail(user.getEmail())){
            user.setPassword(jwtservice.encodePassword(user.getPassword()));
            return repo.save(user);
        }
        else {
            return null;
        }
    }

    public ResponseDTO loginUser(RequestDTO requestDTO){
        User user = repo.findByEmail(requestDTO.email());
        if(user != null){
            if (jwtservice.matchPassword(requestDTO.password(), user.getPassword())) {
                return new ResponseDTO(user.getUserid());
            }
        }
        return null;
    }
}
