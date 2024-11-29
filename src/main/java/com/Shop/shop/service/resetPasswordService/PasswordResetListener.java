package com.Shop.shop.service.resetPasswordService;

import com.Shop.shop.service.UserEmailService;
import com.Shop.shop.model.User;
import com.Shop.shop.service.VerificationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class PasswordResetListener implements ApplicationListener<OnPasswordResetRequestEvent> {
    private final VerificationTokenService verificationTokenService;
    private final UserEmailService userEmailService;

    @Override
    public void onApplicationEvent(OnPasswordResetRequestEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        verificationTokenService.createPasswordResetToken(user, token);

        String email = user.getEmail();
        String subject = "Reset Password";
        String resetUrl = "http://localhost:8080/reset-password-check?token=" + token;
        String message = "Click the link to reset your password: " + resetUrl;

        userEmailService.sendEmail(email, subject, message);
    }
}
