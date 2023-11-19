package com.aminejava.taskmanager.system.controller;

import com.aminejava.taskmanager.dto.user.UserRegisterDto;
import com.aminejava.taskmanager.system.services.ServiceSystem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/taskmanager/v1/intern/management")
public class SystemController {

    private final ServiceSystem serviceSystem;

    public SystemController(ServiceSystem serviceSystem) {
        this.serviceSystem = serviceSystem;
    }

    @PostMapping("/registerSuperAdmin")
    public ResponseEntity<?> registerSuperAdmin(@RequestBody UserRegisterDto userRegisterDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceSystem.registerSuperAdmin(userRegisterDto));
    }
}
