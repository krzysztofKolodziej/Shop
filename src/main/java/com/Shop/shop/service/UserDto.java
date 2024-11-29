package com.Shop.shop.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserDto {
    private String name;
    private String lastname;
    private String email;
    private String phoneNumber;
    private LocalDateTime lastLogin;
}
