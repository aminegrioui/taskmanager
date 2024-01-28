package com.aminejava.taskmanager.system.controller;

import com.aminejava.taskmanager.dto.user.UserLoginDto;
import com.aminejava.taskmanager.services.adminmamagment.AdminAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/taskmanager/v1/authadmin")
public class AdminAuthController {

    private final AdminAuthService adminService;


    public AdminAuthController(AdminAuthService adminService) {
        this.adminService = adminService;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto userLoginDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.login(userLoginDto));
    }
}
