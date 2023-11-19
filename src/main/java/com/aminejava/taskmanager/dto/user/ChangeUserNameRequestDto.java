package com.aminejava.taskmanager.dto.user;

import lombok.Data;

@Data
public class ChangeUserNameRequestDto {
    private String oldUsername;
    private String newUsername;
}
