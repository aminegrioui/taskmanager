package com.aminejava.taskmanager.dto.management.admin;

import lombok.Data;

@Data
public class AddUserDto {
    private String username;
    private String password;
    private String email;
    private boolean isGeneratedCredentials;
}
