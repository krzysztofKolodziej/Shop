package com.Shop.shop.service.registrationService;

import com.Shop.shop.model.User;
import com.Shop.shop.service.UserEmailService;
import com.Shop.shop.service.VerificationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final VerificationTokenService verificationTokenService;
    private UserEmailService userEmailService;
    
    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        verificationTokenService.createVerificationToken(user, token);

        String email = user.getEmail();
        String subject = "Email Verification";
        String confirmationUrl = "http://localhost:8080/verify-email?token=" + token;
        String message = "Click the link to verify your email: " + confirmationUrl;

        userEmailService.sendEmail(email, subject, message);
    }
}
