package com.aminejava.taskmanager.dto.user;

import lombok.Data;

@Data
public class UserResponseDto {

    private String username;
    private Long userId;
    private String description;
}
