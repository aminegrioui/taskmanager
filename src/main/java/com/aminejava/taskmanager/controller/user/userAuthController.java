package com.aminejava.taskmanager.controller.user;

import com.aminejava.taskmanager.dto.user.UserLoginDto;
import com.aminejava.taskmanager.dto.user.UserRegisterDto;
import com.aminejava.taskmanager.services.user.UserAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/taskmanager/v1/auth")

public class userAuthController {

    private final UserAuthService userService;


    public userAuthController(UserAuthService userService) {
        this.userService = userService;
    }


    @PostMapping("/registerUser")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userDto, null));
    }

    @PostMapping("/loginUser")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDto userLoginDto) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(userLoginDto));
    }

    @GetMapping("/validateUserEmail/{token}")
    public ResponseEntity<?> validateEmailAndEnableAccount(@PathVariable String token) {
         return ResponseEntity.status(HttpStatus.OK).body(userService.responseOfValidationEmailLink(token));
    }
}
