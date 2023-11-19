package com.aminejava.taskmanager.dto.management;

import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles;
import lombok.Data;

@Data
public class ManagerRoleRegisterDto {
    private boolean affectManagerRole;
    private boolean generatedCredentials;
    private String username;
    private String password;
    private String email;
    private ApplicationRoles applicationRoles;
}
