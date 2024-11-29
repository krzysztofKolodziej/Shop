package com.Shop.shop.service.registrationService;

import com.Shop.shop.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private final User user;

    public OnRegistrationCompleteEvent(User user) {
        super(user);
        this.user = user;
    }
}
