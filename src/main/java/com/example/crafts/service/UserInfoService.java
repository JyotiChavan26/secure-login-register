package com.example.crafts.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.crafts.entity.UserInfo;
import com.example.crafts.repository.UserInfoRepository;

@Service
public class UserInfoService implements UserDetailsService {

    private final UserInfoRepository repository;
    private final PasswordEncoder encoder;

    public UserInfoService(UserInfoRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserInfo> userOpt = repository.findByEmail(username);

        return userOpt.map(UserInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    // Add new user with validation and roles check
    public String addUser(UserInfo userInfo) {
        // Encode password
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));

        // Validate roles: only allow ROLE_ADMIN, ROLE_USER, ROLE_SELLER
        String roles = userInfo.getRoles();
        if (roles == null || roles.isBlank()) {
            // Default role if not provided
            userInfo.setRoles("ROLE_USER");
        } else {
            // Normalize and validate roles string
            String[] roleArr = roles.split(",");
            StringBuilder validRoles = new StringBuilder();

            for (String role : roleArr) {
                String r = role.trim().toUpperCase();

                // Add ROLE_ prefix if missing
                if (!r.startsWith("ROLE_")) {
                    r = "ROLE_" + r;
                }

                // Check if valid role
                if (r.equals("ROLE_ADMIN") || r.equals("ROLE_USER") || r.equals("ROLE_SELLER")) {
                    if (validRoles.length() > 0) {
                        validRoles.append(",");
                    }
                    validRoles.append(r);
                }
            }

            if (validRoles.length() == 0) {
                // No valid roles found, assign ROLE_USER by default
                userInfo.setRoles("ROLE_USER");
            } else {
                userInfo.setRoles(validRoles.toString());
            }
        }

        repository.save(userInfo);
        return "User Added Successfully";
    }

    // -------------------------------
    // ✅ New method added:
    // -------------------------------
    public UserInfo loadUserByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }
}
