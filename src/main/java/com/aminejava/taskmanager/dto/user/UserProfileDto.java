package com.aminejava.taskmanager.dto.user;

import com.aminejava.taskmanager.dto.userdetails.UserDetailsDto;
import lombok.Data;

@Data
public class UserProfileDto {

    private String username;
    private String email;
    private UserDetailsDto userDetailsDto;
}
