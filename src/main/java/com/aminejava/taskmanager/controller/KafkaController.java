package com.aminejava.taskmanager.controller;

import com.aminejava.taskmanager.KafkaConfig;
import com.aminejava.taskmanager.dto.user.UserRegisterDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private final KafkaConfig kafkaConfig;

    public KafkaController(KafkaConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

    @PostMapping("/test")
    public ResponseEntity<UserRegisterDto> test(@RequestBody UserRegisterDto credentials) {
        kafkaConfig.sendEvent(credentials);
        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }
}
