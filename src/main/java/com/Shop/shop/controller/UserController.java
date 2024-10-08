package com.Shop.shop.controller;

import com.Shop.shop.command.AddUserCommand;
import com.Shop.shop.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> addUser(@RequestBody @Valid AddUserCommand addUserCommand) {
        userService.addUser(addUserCommand);
        return ResponseEntity.status(HttpStatus.OK).body("User successfully added");
    }

    @PutMapping("/{username}")
    public ResponseEntity<String> modifyUser(@RequestBody @Valid AddUserCommand addUserCommand,
                                             @PathVariable String username) {
        userService.modifyUser(username, addUserCommand);
        return ResponseEntity.status(HttpStatus.OK).body("User successfully modified");
    }

}
