package com.web.saree.service;

import com.web.saree.reopository.UserRepository; // Assuming you have a UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Checks if a user with the given email exists in the database.
     *
     * @param email The email address to check.
     * @return true if a user with the email exists, false otherwise.
     */
    public boolean isUserExists(String email) {
        // This is a placeholder. You need to implement the actual database lookup.
        // For example, if you're using Spring Data JPA, your UserRepository
        // might have a method like 'findByEmail'.
        return userRepository.findByEmail(email).isPresent();
    }
}