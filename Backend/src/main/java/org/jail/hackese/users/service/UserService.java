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

    public User registerUser(RequestDTO user){
        if (!repo.existsByEmail(user.email())){
            User save =  new User();
            save.setName(user.name());
            save.setEmail(user.email());
            save.setPassword(jwtservice.encodePassword(user.password()));
            save.setDob(user.dob());
            return repo.save(save);
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
