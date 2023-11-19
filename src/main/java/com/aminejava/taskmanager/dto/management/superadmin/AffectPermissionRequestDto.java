package com.aminejava.taskmanager.dto.management.superadmin;

import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationPermission;
import lombok.Data;

@Data
public class AffectPermissionRequestDto {

    private Long adminId;
    private TogglePermission togglePermission;
    private ApplicationPermission applicationPermission;
}
