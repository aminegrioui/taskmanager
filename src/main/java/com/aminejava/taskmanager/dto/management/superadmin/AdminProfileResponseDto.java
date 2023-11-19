package com.aminejava.taskmanager.dto.management.superadmin;

import com.aminejava.taskmanager.dto.userdetails.UserDetailsDto;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AdminProfileResponseDto {
    private String username;
    private String email;
    private UserDetailsDto userDetailsDto;
}
