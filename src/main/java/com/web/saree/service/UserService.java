package com.web.saree.service;


import com.web.saree.reopository.UserRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class UserService {
    private final UserRepository userRepository;

    public Long findIdByEmail(String email) {
        Long userId = userRepository.findByEmail(email).get().getId ();
        return userId;

    }
     public boolean isUserExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
  
}



