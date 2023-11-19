package com.aminejava.taskmanager.dto.management.superadmin;

import com.aminejava.taskmanager.model.Permission;
import lombok.Data;

import java.util.Set;

@Data
public class AffectPermissionResponseDto {

    private String username;
    private Set<Permission> permissionList;
    private String description;
}
