package com.Shop.shop.controller;

import com.Shop.shop.command.AddUserCommand;
import com.Shop.shop.command.LoginRequest;
import com.Shop.shop.command.UpdateUserCommand;
import com.Shop.shop.model.User;
import com.Shop.shop.service.TokenBlacklistService;
import com.Shop.shop.service.UserDto;
import com.Shop.shop.service.UserService;
import com.Shop.shop.service.registrationService.OnRegistrationCompleteEvent;
import com.Shop.shop.service.VerificationTokenService;
import com.Shop.shop.service.resetPasswordService.OnPasswordResetRequestEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.ApplicationEventPublisher;


@RestController
@AllArgsConstructor
@RequestMapping
public class UserController {

    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final VerificationTokenService verificationTokenService;
    private final TokenBlacklistService tokenBlacklistService;


    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid AddUserCommand addUserCommand) {
        User user = userService.signup(addUserCommand);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
        return ResponseEntity.status(HttpStatus.OK).body("User successfully added");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmailRegistration(@RequestParam("token") String token) {
        String result = verificationTokenService.validateVerificationToken(token);
        if (result.equals("valid")) {
            return ResponseEntity.status(HttpStatus.FOUND).body("Your account has been verified successfully.");
        } else if (result.equals("expired")) {
            return ResponseEntity.status(HttpStatus.GONE).body("Verification token has been expired.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid verification token.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String login = userService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(login);
    }

    @PostMapping("/account/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            String tokenWithoutBearer = token.substring(7); //
            tokenBlacklistService.blacklistToken(tokenWithoutBearer);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok("User has been logged out successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is missing or invalid");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {
        User user = userService.resetPassword(email);
        eventPublisher.publishEvent(new OnPasswordResetRequestEvent(user));
        return ResponseEntity.ok("Password reset link has been sent to your email");
    }

    @PostMapping("/reset-password-check")
    public ResponseEntity<String> resetPasswordCheckToken(@RequestParam String token, @RequestParam String newPassword) {
        String result = verificationTokenService.validateVerificationToken(token);
        if (result.equals("invalid")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid reset token");
        }
        if (result.equals("expired")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reset token has expired");
        }
        User user = userService.resetPasswordCheckToken(token, newPassword);
        if (user != null) {
            return ResponseEntity.ok("Password has been reset successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to reset password");
    }

    @PutMapping("/account/{username}")
    public ResponseEntity<String> modifyUser(@RequestBody(required = false) @Valid UpdateUserCommand updateUserCommand,
                                             @PathVariable String username) {
        userService.modifyUser(username, updateUserCommand);
        return ResponseEntity.status(HttpStatus.OK).body("User successfully modified");
    }

    @GetMapping("/account/details")
    public ResponseEntity<UserDto> getUser(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        UserDto userDetails = userService.getUserDetails(username);
        return ResponseEntity.status(HttpStatus.OK).body(userDetails);

    }

    @DeleteMapping("/account/delete")
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        userService.deleteUser(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User has been deleted");
    }
}

