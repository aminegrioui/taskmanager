package com.aminejava.taskmanager.system.dto;

import lombok.Data;

@Data
public class EmailResponse {
    private String message;
    private boolean isActive;
    private String username;
    private String email;
}
