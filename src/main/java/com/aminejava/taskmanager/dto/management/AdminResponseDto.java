package com.aminejava.taskmanager.dto.management;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AdminResponseDto {

    private String username;
    private String password;
    private String usernameOfAdmin;
    private String description;
    private Long id;
}
