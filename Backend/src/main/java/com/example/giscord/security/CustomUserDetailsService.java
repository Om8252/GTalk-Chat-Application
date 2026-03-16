package com.example.giscord.security;

import com.example.giscord.entity.User;
import com.example.giscord.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepo.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        // simple single role; expand later
        return new CustomUserDetails(u.getUserId(), u.getUserName(), u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

}

