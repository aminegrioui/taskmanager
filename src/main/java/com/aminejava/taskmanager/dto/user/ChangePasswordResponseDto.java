package com.aminejava.taskmanager.dto.user;

import lombok.Data;

@Data
public class ChangePasswordResponseDto {
    private String newPassword;
    private String description;
}
