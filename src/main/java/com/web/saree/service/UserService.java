package com.web.saree.service;

import com.web.saree.entity.Users;
import com.web.saree.reopository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean isUserExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void registerNewUser(String email) {
        if (isUserExists(email)) {
            throw new RuntimeException("User already exists.");
        }
        Users newUser = new Users();
        newUser.setEmail(email);
        userRepository.save(newUser);
    }
}