package com.Shop.shop.service;

import com.Shop.shop.command.AddUserCommand;
import com.Shop.shop.command.LoginRequest;
import com.Shop.shop.command.UpdateUserCommand;
import com.Shop.shop.model.User;
import com.Shop.shop.repository.UserRepository;
import com.Shop.shop.service.emailService.UserEmailService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final long expirationTime;
    private final String secret;
    private final UserEmailService userEmailService;

    public UserService(UserRepository userRepository, UserMapper userMapper,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       @Value("${jwt.expirationTime}") long expirationTime,
                       @Value("${jwt.secret}") String secret, UserEmailService userEmailService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.expirationTime = expirationTime;
        this.secret = secret;
        this.userEmailService = userEmailService;
    }

    public User signup(AddUserCommand addUserCommand) {
        if (userRepository.existsByUsername(addUserCommand.getUsername()) ||
                userRepository.existsByEmail(addUserCommand.getEmail()) ||
                userRepository.existsByPhoneNumber(addUserCommand.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided user exist");
        }
        String hashPassword = passwordEncoder.encode(addUserCommand.getPassword());
        addUserCommand.setPassword(hashPassword);

        return userRepository.save(userMapper.mapUser(addUserCommand));
    }

    public String login(LoginRequest loginRequest) {
        boolean isEmail = loginRequest.getUsernameOrPassword().contains("@");

        User user = userRepository.findByUsernameOrEmail(
                        isEmail ? null : loginRequest.getUsernameOrPassword(),
                        isEmail ? loginRequest.getUsernameOrPassword() : null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not exist"));

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );
        UserDetails principal = (UserDetails) authenticate.getPrincipal();
        return JWT.create()
                .withSubject(principal.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(Algorithm.HMAC256(secret));
    }

    public void modifyUser(String username, UpdateUserCommand updateUserCommand) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not exist"));

        if ((userRepository.existsByUsername(updateUserCommand.getUsername()) && !user.getUsername().equals(updateUserCommand.getUsername())) ||
                (userRepository.existsByEmail(updateUserCommand.getEmail()) && !user.getEmail().equals(updateUserCommand.getEmail())) ||
                userRepository.existsByPhoneNumber(updateUserCommand.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided user exist");
        }

        String hashPassword = passwordEncoder.encode(updateUserCommand.getNewPassword());
        updateUserCommand.setNewPassword(hashPassword);

        userRepository.save(userMapper.mapUserModify(updateUserCommand, user));
    }
}
