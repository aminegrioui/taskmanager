package com.aminejava.taskmanager.controller.user;

import com.aminejava.taskmanager.dto.user.UserLoginDto;
import com.aminejava.taskmanager.dto.user.UserRegisterDto;
import com.aminejava.taskmanager.services.user.UserAuthService;
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
    public String registerUser(@RequestBody UserRegisterDto userDto) {
        return userService.registerUser(userDto);
    }

    @PostMapping("/loginUser")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDto userLoginDto) throws Exception {
        return userService.login(userLoginDto);
    }
}
