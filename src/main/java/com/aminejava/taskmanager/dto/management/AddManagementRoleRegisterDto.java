package com.aminejava.taskmanager.dto.management;

import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles;
import lombok.Data;

@Data
public class AddManagementRoleRegisterDto {
    private String username;
    private String password;
    private String email;
    private boolean generatedCredentials;
    private ApplicationRoles applicationRoles;
}
