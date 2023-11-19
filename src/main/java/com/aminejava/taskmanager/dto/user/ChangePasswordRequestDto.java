package com.aminejava.taskmanager.dto.user;

import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    private String oldUPassword;
    private String newPassword;
}
