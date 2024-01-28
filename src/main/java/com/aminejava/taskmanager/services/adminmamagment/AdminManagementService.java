package com.aminejava.taskmanager.services.adminmamagment;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.management.*;
import com.aminejava.taskmanager.dto.management.admin.AddUserDto;
import com.aminejava.taskmanager.dto.management.superadmin.*;
import com.aminejava.taskmanager.dto.project.ProjectResponseDto;
import com.aminejava.taskmanager.dto.task.TaskResponseDto;
import com.aminejava.taskmanager.dto.user.*;
import com.aminejava.taskmanager.exception.ResourceNotFoundException;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.exception.user.AlreadyExistUserException;
import com.aminejava.taskmanager.model.Permission;
import com.aminejava.taskmanager.model.SubTask;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.BlacklistEntryRepository;
import com.aminejava.taskmanager.repository.PermissionRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.jwt.JwtTool;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles;
import com.aminejava.taskmanager.services.project.ProjectConverter;
import com.aminejava.taskmanager.services.project.ProjectService;
import com.aminejava.taskmanager.services.subtasks.SubTaskService;
import com.aminejava.taskmanager.services.tasks.TaskService;
import com.aminejava.taskmanager.services.user.UserAuthService;
import com.aminejava.taskmanager.services.user.UserService;
import com.aminejava.taskmanager.system.entities.BlacklistEntry;
import com.aminejava.taskmanager.system.services.SystemTaskManager;
import com.google.common.base.Strings;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminManagementService {

    private final AdminAuthService adminAuthService;
    private final ProjectService projectService;
    private final UserService userService;
    private final AdminRepository adminRepository;
    private final TaskService taskService;
    private final SubTaskService subTaskService;
    private final SystemTaskManager systemTaskManager;
    private final ProjectConverter projectConverter;
    private final JwtTool jwtTool;
    private final PermissionRepository permissionRepository;
    private final UserAuthService userAuthService;
    private final AppTool appTool;


    public AdminManagementService(AdminAuthService adminAuthService, ProjectService projectService, UserService userService,
                                  AdminRepository adminRepository, TaskService taskService, SubTaskService subTaskService, SystemTaskManager systemTaskManager, ProjectConverter projectConverter, JwtTool jwtTool,
                                  PermissionRepository permissionRepository, UserAuthService userAuthService, AppTool appTool) {
        this.adminAuthService = adminAuthService;
        this.projectService = projectService;
        this.userService = userService;
        this.adminRepository = adminRepository;
        this.taskService = taskService;

        this.subTaskService = subTaskService;
        this.systemTaskManager = systemTaskManager;
        this.projectConverter = projectConverter;
        this.jwtTool = jwtTool;
        this.permissionRepository = permissionRepository;
        this.userAuthService = userAuthService;
        this.appTool = appTool;
    }


    public AdminResponseDto registerManagerRole(AddManagementRoleRegisterDto addManagementRoleRegisterDto, HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        return adminAuthService.registerManagerRole(addManagementRoleRegisterDto, parseTokenResponse.getId(), parseTokenResponse.getUsername());
    }

    public List<ProjectResponseDto> getAllProjects(HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        return projectService.getAllProjects(httpHeaders);
    }

    public List<UserResponseDtoForGetAll> getAllUsers(boolean isAll, HttpHeaders httpHeaders) {
        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        if (isAll) {
            return userService.getAllUsersShorts();
        }
        return userService.getAllUsersShorts().stream().
                filter(userGetAllResponseDto -> userGetAllResponseDto.getUsernameOfAdmin() != null && userGetAllResponseDto.getIdOfAdmin().longValue()
                        == parseTokenResponse.getId())
                .collect(Collectors.toList());
    }

    public List<UserWithAllDetailsResponseDtoForGetAll> getAllUsersWithDetails(boolean isAll, HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        if (isAll) {
            return userService.getAllUsers()
                    .stream()
                    .map(user -> new UserWithAllDetailsResponseDtoForGetAll(
                            user.getUserDetails(), user.getUsername(), user.getRole(),
                            user.isAccountNonLocked(), user.isCredentialsNonExpired(),
                            user.isAccountNonExpired(), user.getEmail(), user.getProjects().stream().map(projectConverter::convertToProjectResponseDto).collect(Collectors.toSet()),
                            user.getAdmin() != null ? user.getAdmin().getUsername() : null, user.getZonedDateTimeLockedUser())).collect(Collectors.toList());
        }
        return userService.getAllUsers()
                .stream()
                .filter(user -> user.getAdmin() != null && user.getAdmin().getAdminId().longValue() == parseTokenResponse.getId())
                .map(user -> new UserWithAllDetailsResponseDtoForGetAll(
                        user.getUserDetails(), user.getUsername(), user.getRole(),
                        user.isAccountNonLocked(), user.isCredentialsNonExpired(),
                        user.isAccountNonExpired(), user.getEmail(), user.getProjects().stream().map(projectConverter::convertToProjectResponseDto).collect(Collectors.toSet()),
                        user.getAdmin() != null ? user.getAdmin().getUsername() : null, user.getZonedDateTimeLockedUser())).collect(Collectors.toList());
    }

    public List<TaskResponseDto> getAllTasks(HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        return taskService.getAllTasks(httpHeaders);
    }

    public List<SubTask> getAllSubTasks(HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        return subTaskService.getAllSubTasks(httpHeaders);
    }

    public List<Admin> getAllManagersRole(HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        return adminRepository.findAll().stream().filter(admin ->
                admin.getPermissions().stream().
                        filter(permission -> permission.getPermission().equals("ROLE_" + ApplicationRoles.SUPER_ADMIN)).findFirst().isEmpty()).collect(Collectors.toList());
    }

    public UserResponse disableUser(ToggleUser toggleUser, HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        return userService.disableUser(toggleUser);
    }

    public DeleteUserResponseDto deleteUser(Long id, HttpHeaders httpHeaders) {
        return userService.deleteUser(id, httpHeaders, null);
    }

    @Transactional
    public DeleteManagerRoleResponseDto deleteManagerRole(Long id, HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(id);

        if (optionalAdmin.isEmpty() || optionalAdmin.get().isDeleted()) {
            throw new ResourceNotFoundException("This manager role with id: " + id + " is not found or is deleted");
        }

        if (optionalAdmin.get().getPermissions().stream().anyMatch(permission -> permission.getPermission().equals("ROLE_" + ApplicationRoles.SUPER_ADMIN))) {
            return new DeleteManagerRoleResponseDto(false, "This manager role with username: " + optionalAdmin.get().getUsername()
                    + " is " + ApplicationRoles.SUPER_ADMIN + "  and you can't delete this user, contact support ");
        }
        optionalAdmin.get().setDeleted(true);
        optionalAdmin.get().setPermissions(null);
        adminRepository.save(optionalAdmin.get());
        appTool.saveNewBlackListWithUsername(optionalAdmin.get().getUsername(),"DELETED_MANAGER_ROLE");
        return new DeleteManagerRoleResponseDto(true, "The Manager with username: " + optionalAdmin.get().getUsername() + " is deleted ");
    }

    @Transactional
    public DeleteManagerRoleResponseDto deleteSuperAdmin(Long id, HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(id);

        if (optionalAdmin.isEmpty() || optionalAdmin.get().isDeleted()) {
            throw new ResourceNotFoundException("This super Admin with id: " + id + " is not found or is deleted");
        }
        if (id.longValue() == parseTokenResponse.getId()) {
            throw new ResourceNotFoundException("You can't delete your Account with this Api. Use Your Profile APIs to do that");
        }
        Admin admin = optionalAdmin.get();
        admin.setDeleted(true);

        // if this deleted is live logg in
        appTool.saveNewBlackListWithUsername(admin.getUsername(),"DELETED_SUPER_ADMIN");
        return new DeleteManagerRoleResponseDto(true, "The Super Admin with username: " + optionalAdmin.get().getUsername() + " is deleted ");
    }

    public AffectRoleResponseDto affectManagerRole(AffectRoleRequestDto affectRoleRequestDto, HttpHeaders httpHeaders) throws AlreadyExistUserException, ValidationDataException {
        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        if (affectRoleRequestDto.getApplicationRoles() == null || affectRoleRequestDto.getIdManager() == null) {
            throw new ValidationDataException("The given data are incorrect ");
        }

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(affectRoleRequestDto.getIdManager());

        if (optionalAdmin.isEmpty() || optionalAdmin.get().isDeleted()) {
            throw new ResourceNotFoundException("This manager role with id: " + affectRoleRequestDto.getIdManager() + " is not found or is deleted");
        }
        if (optionalAdmin.get().getPermissions().stream().anyMatch(permission -> permission.getPermission().equals("ROLE_" + affectRoleRequestDto.getApplicationRoles().name()))) {
            throw new ValidationDataException("This manager/admin with username: " + optionalAdmin.get().getUsername() + " is already " + affectRoleRequestDto.getApplicationRoles().name());
        }


        Admin manager = optionalAdmin.get();

        AddManagementRoleRegisterDto addManagementRoleRegisterDto = new AddManagementRoleRegisterDto();
        // send email to the Admin. He has new

        addManagementRoleRegisterDto.setEmail(manager.getEmail());
        addManagementRoleRegisterDto.setApplicationRoles(affectRoleRequestDto.getApplicationRoles());
        if (!Strings.isNullOrEmpty(affectRoleRequestDto.getPassword()) && !Strings.isNullOrEmpty(affectRoleRequestDto.getUsername())) {
            addManagementRoleRegisterDto.setUsername(affectRoleRequestDto.getUsername());
            addManagementRoleRegisterDto.setPassword(affectRoleRequestDto.getPassword());
        } else {
            addManagementRoleRegisterDto.setGeneratedCredentials(true);
        }

        // delete the old manager/admin

        DeleteManagerRoleResponseDto deleteManagerRoleResponseDto = this.deleteManagerRole(affectRoleRequestDto.getIdManager(), httpHeaders);

        // Register manager
        AdminResponseDto adminResponseDto = this.adminAuthService.registerManagerRole(addManagementRoleRegisterDto, parseTokenResponse.getId(), parseTokenResponse.getUsername());

        AffectRoleResponseDto affectRoleResponseDto = new AffectRoleResponseDto();
        affectRoleResponseDto.setAdminResponseDto(adminResponseDto);
        affectRoleResponseDto.setDeleteManagerRoleResponseDto(deleteManagerRoleResponseDto);
        return affectRoleResponseDto;

    }

    public AffectPermissionResponseDto affectPermissions(AffectPermissionRequestDto affectPermissionRequestDto, HttpHeaders httpHeaders) {
        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        if (affectPermissionRequestDto.getApplicationPermission() == null || affectPermissionRequestDto.getAdminId() == null) {
            throw new ValidationDataException("The given data are incorrect ");
        }

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(affectPermissionRequestDto.getAdminId());

        if (optionalAdmin.isEmpty() || optionalAdmin.get().isDeleted()) {
            throw new ResourceNotFoundException("This manager role with id: " + affectPermissionRequestDto.getAdminId() + " is not found or is deleted");
        }
        Admin admin = optionalAdmin.get();
        AffectPermissionResponseDto affectPermissionResponseDto = new AffectPermissionResponseDto();
        affectPermissionResponseDto.setUsername(admin.getUsername());
        Set<Permission> permissionSet = admin.getPermissions();
        Permission permission;
        if (affectPermissionRequestDto.getTogglePermission().equals(TogglePermission.ADD_PERMISSION)) {
            if (admin.getPermissions().stream().anyMatch(p -> p.getPermission().equals(affectPermissionRequestDto.getApplicationPermission().getName()))) {
                throw new ValidationDataException("This manager/admin with username: "
                        + optionalAdmin.get().getUsername() + " has already permission " + affectPermissionRequestDto.getApplicationPermission().name());
            }

            permission = permissionRepository.findByPermission(affectPermissionRequestDto.getApplicationPermission().getName());
            permissionSet.add(permission);
            admin.setPermissions(permissionSet);
            adminRepository.save(admin);
            affectPermissionResponseDto.setPermissionList(permissionSet);
            affectPermissionResponseDto.setDescription("The admin: " + admin.getUsername() + " has now a new permission: " + affectPermissionRequestDto.getApplicationPermission());
            return affectPermissionResponseDto;
        }
        if (admin.getPermissions().stream().noneMatch(p -> p.getPermission().equals(affectPermissionRequestDto.getApplicationPermission().getName()))) {
            throw new ValidationDataException("This manager/admin with username: "
                    + optionalAdmin.get().getUsername() + " has already no permission " + affectPermissionRequestDto.getApplicationPermission().name());
        }
        permission = permissionRepository.findByPermission(affectPermissionRequestDto.getApplicationPermission().getName());
        permissionSet.remove(permission);
        admin.setPermissions(permissionSet);
        adminRepository.save(admin);
        affectPermissionResponseDto.setPermissionList(permissionSet);
        affectPermissionResponseDto.setDescription("The admin: " + admin.getUsername() + " doesn't have now this permission: " + affectPermissionRequestDto.getApplicationPermission());
        appTool.saveNewBlackListWithUsername(optionalAdmin.get().getUsername(),TogglePermission.DELETE_PERMISSION.name());
        return affectPermissionResponseDto;
    }

    public AdminResponseDto saveUser(AddUserDto addUserDto, HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        AdminResponseDto adminResponseDto = new AdminResponseDto();
        String username;
        String password;
        if (addUserDto.isGeneratedCredentials()) {
            username = RandomStringUtils.random(10, true, false);
            password = RandomStringUtils.random(20, true, true);
            adminResponseDto.setPassword(password);
        } else {
            username = addUserDto.getUsername();
            adminResponseDto.setUsername(username);
            password = addUserDto.getPassword();
        }


        Long idManager = parseTokenResponse.getId();
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setUsername(username);
        userRegisterDto.setPassword(password);
        userRegisterDto.setEmail(addUserDto.getEmail());

        UserResponseDto userResponse = userAuthService.registerUser(userRegisterDto, idManager);
        adminResponseDto.setDescription(userResponse.getDescription());
        adminResponseDto.setId(userResponse.getUserId());
        adminResponseDto.setUsernameOfAdmin(parseTokenResponse.getUsername());
        adminResponseDto.setUsername(userResponse.getUsername());
        return adminResponseDto;
    }
}
