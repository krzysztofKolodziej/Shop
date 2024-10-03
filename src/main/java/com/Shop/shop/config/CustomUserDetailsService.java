package com.Shop.shop.config;

import com.Shop.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        boolean isEmail = usernameOrEmail.contains("@");

        com.Shop.shop.model.User user = userRepository.findByUsernameOrEmail(
                        isEmail ? null : usernameOrEmail,
                        isEmail ? usernameOrEmail : null)
                .orElseThrow(() -> new UsernameNotFoundException("User details not found for the user: " + usernameOrEmail));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
