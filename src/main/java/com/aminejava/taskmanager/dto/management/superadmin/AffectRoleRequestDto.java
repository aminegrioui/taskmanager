package com.aminejava.taskmanager.dto.management.superadmin;

import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles;
import lombok.Data;

@Data
public class AffectRoleRequestDto {

    private Long idManager;
    private String password;
    private String username;
    private ApplicationRoles applicationRoles;
}
