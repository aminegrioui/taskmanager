package com.aminejava.taskmanager.controller.user;

import com.aminejava.taskmanager.KafkaConfig;
import com.aminejava.taskmanager.dto.user.UserLoginDto;
import com.aminejava.taskmanager.dto.user.UserRegisterDto;
import com.aminejava.taskmanager.services.user.UserAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/taskmanager/v1/auth")

public class UserAuthController {

    private final UserAuthService userService;

    private final KafkaConfig kafkaConfig;

    public UserAuthController(UserAuthService userService, KafkaConfig kafkaConfig) {
        this.userService = userService;
        this.kafkaConfig = kafkaConfig;
    }

    @PostMapping("/kafka/test")
    public ResponseEntity<UserRegisterDto> test(@RequestBody UserRegisterDto credentials) {
        kafkaConfig.sendEvent(credentials);
        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
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
