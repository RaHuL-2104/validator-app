package com.rahul.validator.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import java.util.ArrayList;
import com.rahul.validator.repository.UserRepository;
import com.rahul.validator.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository;

    @Override 
    public UserDetails loadUserByUsername(String email)throws UsernameNotFoundException{
        UserEntity existingUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Email not found for the email: " + email));
        return new User(existingUser.getEmail(), existingUser.getPassword(), new ArrayList<>());

    }
}
