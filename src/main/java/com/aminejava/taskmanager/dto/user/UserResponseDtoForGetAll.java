package com.aminejava.taskmanager.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDtoForGetAll {
    private String username;
    private String email;
    private String usernameOfAdmin;
    private Long idOfAdmin;
}
