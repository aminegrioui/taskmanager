package com.aminejava.taskmanager.system.services;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.user.UserRegisterDto;
import com.aminejava.taskmanager.dto.user.UserResponseDto;
import com.aminejava.taskmanager.exception.ResourceNotFoundException;
import com.aminejava.taskmanager.exception.user.AlreadyExistUserException;
import com.aminejava.taskmanager.exception.user.EmailValidationException;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.Permission;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.PermissionRepository;
import com.aminejava.taskmanager.repository.TaskManagerAdminLoggerRepository;
import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationPermission;
import com.aminejava.taskmanager.system.entities.TaskManagerAdminHistoric;
import com.google.common.base.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ServiceSystem {

    private final AppTool appTool;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final PermissionRepository permissionRepository;
    private final TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository;

    public ServiceSystem(AppTool appTool, PasswordEncoder passwordEncoder, AdminRepository adminRepository,
                         PermissionRepository permissionRepository, TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository) {
        this.appTool = appTool;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.permissionRepository = permissionRepository;
        this.taskManagerAdminHistoricRepository = taskManagerAdminHistoricRepository;
    }

    @Transactional
    public UserResponseDto registerSuperAdmin(UserRegisterDto adminDto) {
        UserResponseDto userResponseDto = new UserResponseDto();
        if (Strings.isNullOrEmpty(adminDto.getEmail()) || Strings.isNullOrEmpty(adminDto.getUsername()) || Strings.isNullOrEmpty(adminDto.getPassword())) {
            throw new ValidationDataException("To Register a new SuperAdmin you have to give username, email and password ");
        }
        if (!appTool.validUserName(adminDto.getUsername())) {
            throw new ValidationDataException("The given username is not valid. It must have at minimum 8 aplha character !!");
        }
        if (!appTool.validUserPassword(adminDto.getPassword())) {
            throw new ValidationDataException("The given password is not valid. It must have at minimum 10 alphanumeric  !!");
        }
        if (!appTool.checkValidationOfGivenEmail(adminDto.getEmail())) {
            throw new EmailValidationException("This email is not valid: " + adminDto.getEmail());
        }
        // Check if the user is valid

        Optional<Admin> optionalAdmin = adminRepository.findByUsernameOrEmailAndDeleted(adminDto.getUsername(), adminDto.getEmail(),false);

        if (optionalAdmin.isEmpty()) {
            TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(adminDto.getUsername(), "REGISTER_SUPER_ADMIN");
            Admin admin = new Admin();
            admin.setUsername(adminDto.getUsername());
            admin.setPassword(passwordEncoder.encode(adminDto.getPassword()));
            admin.setEmail(adminDto.getEmail());
            admin.setEnabled(true);
            admin.setCredentialsNonExpired(true);
            admin.setAccountNonLocked(true);
            admin.setAccountNonExpired(true);
            List<ApplicationPermission> applicationPermissions = appTool.getAllApplicationPermissions();
            Set<Permission> permissionSetOfSuperAdmin = new HashSet<>();
            Permission permission = permissionRepository.findByPermission("ROLE_SUPER_ADMIN");
            permissionSetOfSuperAdmin.add(permission);
            for (ApplicationPermission applicationPermission : applicationPermissions) {
                permission = permissionRepository.findByPermission(applicationPermission.getName());
                permissionSetOfSuperAdmin.add(permission);
            }
            admin.setPermissions(permissionSetOfSuperAdmin);

            adminRepository.save(admin);
            userResponseDto.setUsername(adminDto.getUsername());
            userResponseDto.setDescription("Super Admin with userName " + adminDto.getUsername() + " was created");
            taskManagerAdminHistoric.setResponseBody("Super Admin with userName " + adminDto.getUsername() + " was registered");
            taskManagerAdminHistoric.setSuccessOperation(true);
            taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);
            return userResponseDto;
        }
        throw new AlreadyExistUserException("Super Admin with this userName and email: " + adminDto.getUsername() + " was already existed ");
    }

    @Transactional
    public UserResponseDto disableSuperAdmin(Long id) {

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(id);
        UserResponseDto userResponseDto = new UserResponseDto();
        if (optionalAdmin.isPresent()) {
            TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(optionalAdmin.get().getUsername(), "DISABLE_SUPER_ADMIN");
            optionalAdmin.get().setEnabled(false);
            userResponseDto.setDescription("This superAdmin+ " + optionalAdmin.get().getUsername() + " is disabled ");
            userResponseDto.setUsername(optionalAdmin.get().getUsername());
            taskManagerAdminHistoric.setSuccessOperation(true);
            taskManagerAdminHistoric.setResponseBody("This superAdmin+ " + optionalAdmin.get().getUsername() + " is disabled ");
            taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);
            return userResponseDto;
        }
        throw new ResourceNotFoundException("This superAdmin is not found ");
    }

}
