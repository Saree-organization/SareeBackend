package com.web.saree.security;

import com.web.saree.entity.Users;
import com.web.saree.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Create a Spring Security UserDetails object using the email from the Users entity.
        // The password field is empty because this is an OTP-based authentication system.
        // We're also using an empty list for authorities for simplicity.
        return new org.springframework.security.core.userdetails.User(user.getEmail(), "", new ArrayList<>());
    }
}