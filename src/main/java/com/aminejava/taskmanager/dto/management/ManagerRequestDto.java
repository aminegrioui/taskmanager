package com.aminejava.taskmanager.dto.management;


import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationPermission;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ManagerRequestDto {
    private String username;
    private String password;
    private List<ApplicationPermission> applicationPermissions = new ArrayList<>();
    private boolean isGeneratedCredentials;
}
