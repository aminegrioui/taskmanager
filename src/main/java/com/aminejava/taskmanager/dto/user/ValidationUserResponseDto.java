package com.aminejava.taskmanager.dto.user;

import lombok.Data;

@Data
public class ValidationUserResponseDto {
    private boolean isAlreadyValid;
    private boolean isValid;
    private String message;
}
