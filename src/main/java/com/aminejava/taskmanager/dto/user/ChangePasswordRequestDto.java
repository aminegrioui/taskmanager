package com.aminejava.taskmanager.dto.user;

import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    private String oldPassword;
    private String newPassword;
}
