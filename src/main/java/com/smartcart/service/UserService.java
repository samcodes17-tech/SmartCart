package com.smartcart.service;

import com.smartcart.dto.RegisterForm;
import com.smartcart.entity.Role;
import com.smartcart.entity.User;
import com.smartcart.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(RegisterForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User();
        user.setName(form.getName());
        user.setEmail(form.getEmail());
        // Never store the raw password. BCrypt turns it into a one-way hash;
        // even if the database is leaked, the original password can't be recovered from it.
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        // Everyone who self-registers becomes a CUSTOMER.
        // Admin accounts are created separately (e.g. directly in the database),
        // not through the public registration form.
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
