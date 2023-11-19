package com.aminejava.taskmanager.dto.management;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminResponseForGetAllDto {
    private String username;
    private String email;
    private String usernameOfAdmin;
    private Long id;
    private Long idOfAdmin;
}
