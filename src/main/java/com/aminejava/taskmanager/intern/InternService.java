package com.aminejava.taskmanager.intern;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.management.AdminResponseDto;
import com.aminejava.taskmanager.model.Permission;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.PermissionRepository;
import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationPermission;
import com.google.common.base.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

import static com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles.SUPER_ADMIN;

@Service
public class InternService {

    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final AdminRepository adminRepository;
    private final AppTool appTool;

    public InternService(PasswordEncoder passwordEncoder, PermissionRepository permissionRepository, AdminRepository adminRepository, AppTool appTool) {
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.adminRepository = adminRepository;
        this.appTool = appTool;
    }


    public AdminResponseDto RegisterSuperAdmin(SuperAdminRegistrationDto superAdminRegistrationDto) {
        String username;
        String password;
        AdminResponseDto adminResponseDto = new AdminResponseDto();

        username = superAdminRegistrationDto.getUsername();
        password = superAdminRegistrationDto.getPassword();

        if (!Strings.isNullOrEmpty(username)) {
            adminResponseDto.setDescription("to register Super Admin you have to give a username ");
        }

        if (!Strings.isNullOrEmpty(password)) {
            adminResponseDto.setDescription("to register Super Admin you have to give a password ");
            return adminResponseDto;
        }
        Optional<Admin> optionalAdmin = adminRepository.findAdminByUsername(username);
        if (optionalAdmin.isPresent()) {
            adminResponseDto.setDescription("This username is already used. Give please another username ");
            return adminResponseDto;
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        if (!Strings.isNullOrEmpty(superAdminRegistrationDto.getEmail())) {
            admin.setEmail(superAdminRegistrationDto.getEmail());
        }

        admin.setEnabled(true);
        admin.setCredentialsNonExpired(true);
        admin.setAccountNonLocked(true);
        admin.setAccountNonExpired(true);

        HashSet<Permission> permissions = new HashSet<>();
        Permission permission;
        for (ApplicationPermission applicationPermission : appTool.getAllApplicationPermissions()) {
            permission = permissionRepository.findByPermission(applicationPermission.getName());
            permissions.add(permission);
        }
        permission = permissionRepository.findByPermission("ROLE" + SUPER_ADMIN);
        permissions.add(permission);
        admin.setPermissions(permissions);
        adminRepository.save(admin);

        adminResponseDto.setUsername(username);
        adminResponseDto.setDescription("The Super Admin: " + username + " is registered  successfully: ");
        return adminResponseDto;
    }
}
