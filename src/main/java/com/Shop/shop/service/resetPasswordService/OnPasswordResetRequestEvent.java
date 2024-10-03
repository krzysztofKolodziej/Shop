package com.Shop.shop.service.resetPasswordService;

import com.Shop.shop.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnPasswordResetRequestEvent extends ApplicationEvent {
    private final User user;

    public OnPasswordResetRequestEvent(User user) {
        super(user);
        this.user = user;
    }
}
