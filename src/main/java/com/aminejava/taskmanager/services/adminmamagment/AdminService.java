package com.aminejava.taskmanager.services.adminmamagment;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.management.AddManagementRoleRegisterDto;
import com.aminejava.taskmanager.dto.management.AdminResponseDto;
import com.aminejava.taskmanager.dto.management.AdminResponseForGetAllDto;
import com.aminejava.taskmanager.dto.management.DeleteManagerRoleResponseDto;
import com.aminejava.taskmanager.dto.user.DeleteUserResponseDto;
import com.aminejava.taskmanager.exception.ResourceNotFoundException;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.TaskManagerAdminLoggerRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.jwt.JwtTool;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles;
import com.aminejava.taskmanager.services.user.UserService;
import com.aminejava.taskmanager.system.entities.TaskManagerAdminHistoric;
import com.aminejava.taskmanager.system.services.SystemTaskManager;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserService userService;
    private final AdminRepository adminRepository;
    private final JwtTool jwtTool;
    private final AdminAuthService adminAuthService;
    private final SystemTaskManager systemTaskManager;
    private final TaskManagerAdminLoggerRepository taskManagerAdminLoggerRepository;
    private final AppTool appTool;

    public AdminService(UserService userService, AdminRepository adminRepository,
                        JwtTool jwtGenerator, AdminAuthService adminAuthService, SystemTaskManager systemTaskManager, TaskManagerAdminLoggerRepository taskManagerAdminLoggerRepository, AppTool appTool) {
        this.userService = userService;
        this.adminRepository = adminRepository;
        this.jwtTool = jwtGenerator;
        this.adminAuthService = adminAuthService;
        this.systemTaskManager = systemTaskManager;
        this.taskManagerAdminLoggerRepository = taskManagerAdminLoggerRepository;
        this.appTool = appTool;
    }


    public AdminResponseDto addManager(AddManagementRoleRegisterDto addManagementRoleRegisterDto, HttpHeaders httpHeaders) {
        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        // Log the api
        addManagementRoleRegisterDto.setApplicationRoles(ApplicationRoles.MANAGER);
        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "ADD_MANAGER");
        AdminResponseDto adminResponseDto = adminAuthService.registerManagerRole(addManagementRoleRegisterDto, parseTokenResponse.getId(), parseTokenResponse.getUsername());
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoric.setResponseBody(adminResponseDto.toString());
        taskManagerAdminLoggerRepository.save(taskManagerAdminHistoric);
        return adminResponseDto;
    }

    public List<AdminResponseForGetAllDto> getAllCreatedManagers(HttpHeaders httpHeaders) {
        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "ALL_MANAGERS");
        List<AdminResponseForGetAllDto> adminList = adminRepository.findAll().stream().filter(admin ->
                        !admin.isDeleted() && admin.getRole() != null
                                && admin.getRole().equals("ROLE_MANAGER") && admin.getCreatedId() == parseTokenResponse.getId())
                .map(admin -> new AdminResponseForGetAllDto(admin.getUsername(), admin.getEmail(), parseTokenResponse.getUsername(), admin.getAdminId(), admin.getCreatedId()))
                .collect(Collectors.toList());
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoric.setResponseBody("ALL_MANAGERS");
        taskManagerAdminLoggerRepository.save(taskManagerAdminHistoric);
        return adminList;
    }

    @Transactional
    public DeleteManagerRoleResponseDto deleteManager(Long id, HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(id);

        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "DELETE_ADMIN_ROLE");
        if (optionalAdmin.isEmpty() || optionalAdmin.get().isDeleted()) {
            taskManagerAdminHistoric.setSuccessOperation(false);
            taskManagerAdminHistoric.setResponseBody("This Manager with id: " + id + " is not found or is deleted");
            taskManagerAdminLoggerRepository.save(taskManagerAdminHistoric);
            throw new ResourceNotFoundException("This Manager with id: " + id + " is not found or is deleted");
        }
        if (optionalAdmin.get().getCreatedId() != parseTokenResponse.getId()) {
            taskManagerAdminHistoric.setSuccessOperation(false);
            taskManagerAdminHistoric.setResponseBody("You have not created this manager, you can delete manager who you have created ");
            taskManagerAdminLoggerRepository.save(taskManagerAdminHistoric);
            throw new ValidationDataException("You have not created this manager, you can delete manager who you have created ");
        }
        // delete projects of this manager
        optionalAdmin.get().getProjectManagers().stream().forEach(projectManager -> projectManager.setDeleted(true));

        // remove affetation of this projects with users
        optionalAdmin.get().getProjectManagers().stream().forEach(projectManager -> projectManager.setUsers(null));

        // delete Manager
        optionalAdmin.get().setDeleted(true);

        DeleteManagerRoleResponseDto deleteManagerRoleResponseDto = new DeleteManagerRoleResponseDto(true, "The Manager with username: " + optionalAdmin.get().getUsername() + " is deleted ");
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoric.setResponseBody("The Manager with username: " + optionalAdmin.get().getUsername() + " is deleted ");
        taskManagerAdminLoggerRepository.save(taskManagerAdminHistoric);
        return deleteManagerRoleResponseDto;
    }

    public DeleteUserResponseDto deleteUser(Long id, HttpHeaders httpHeaders) {
        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "DELETE_USER_ROLE");
        DeleteUserResponseDto deleteUserResponseDto = userService.deleteUser(id, httpHeaders, "ADMIN");
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoric.setResponseBody(deleteUserResponseDto.getDescription());
        taskManagerAdminLoggerRepository.save(taskManagerAdminHistoric);
        return userService.deleteUser(id, httpHeaders, null);
    }


    // getAll Projects that a manager has created

    // getAll Users that a manager has affected to work on the projects
}
