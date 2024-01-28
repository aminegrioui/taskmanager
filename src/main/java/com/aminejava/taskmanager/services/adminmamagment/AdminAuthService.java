package com.aminejava.taskmanager.services.adminmamagment;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.management.AdminResponseDto;
import com.aminejava.taskmanager.dto.management.AddManagementRoleRegisterDto;
import com.aminejava.taskmanager.dto.user.LoginResponseDto;
import com.aminejava.taskmanager.dto.user.UserLoginDto;
import com.aminejava.taskmanager.exception.GlobalException;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.exception.user.AlreadyExistUserException;
import com.aminejava.taskmanager.exception.user.EmailValidationException;
import com.aminejava.taskmanager.exception.user.UserLockoutException;
import com.aminejava.taskmanager.exception.user.UserNameNotFoundException;
import com.aminejava.taskmanager.model.Permission;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.PermissionRepository;
import com.aminejava.taskmanager.repository.TaskManagerAdminLoggerRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationPermission;
import com.aminejava.taskmanager.securityconfig.userdeatails.models.ApplicationUserDetails;
import com.aminejava.taskmanager.system.entities.TaskManagerAdminHistoric;
import com.google.common.base.Strings;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles.*;


@Service
public class AdminAuthService {

    public static final String ROLE = "ROLE_";
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final AppTool appTool;
    private final TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository;


    public AdminAuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, PermissionRepository permissionRepository, AuthenticationManager authenticationManager, JwtGenerator jwtGenerator, AppTool appTool,
                            TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;

        this.appTool = appTool;
        this.taskManagerAdminHistoricRepository = taskManagerAdminHistoricRepository;
    }

    public AdminResponseDto registerManagerRole(AddManagementRoleRegisterDto addManagementRoleRegisterDto, long createdAdminId, String usernameAdminOfCreated) {
        String username;
        String password;
        String email = addManagementRoleRegisterDto.getEmail();
        AdminResponseDto adminResponseDto = new AdminResponseDto();
        if (addManagementRoleRegisterDto.isGeneratedCredentials()) {
            // send ihm an email
            username = RandomStringUtils.random(8, true, false);
            password = RandomStringUtils.random(10, true, true);

            // send email to him
            adminResponseDto.setPassword(password);
        } else {
            username = addManagementRoleRegisterDto.getUsername();
            password = addManagementRoleRegisterDto.getPassword();


            if (Strings.isNullOrEmpty(username)
                    || Strings.isNullOrEmpty(password)
                    || Strings.isNullOrEmpty(email)
                    || addManagementRoleRegisterDto.getApplicationRoles() == null) {
                throw new ValidationDataException("To Register a new "
                        + addManagementRoleRegisterDto.getApplicationRoles() +
                        " you have to give username, email password and role: SUPER_ADMIN,ADMIN, MANAGER ");
            }
            if (!appTool.validUserName(username)) {
                throw new ValidationDataException("The given username is not valid. It must have at minimum 8 aplha character !!");
            }
            if (!appTool.validUserPassword(password)) {
                throw new ValidationDataException("The given password is not valid. It must have at minimum 10 alphanumeric  !!");
            }
        }
        if (!appTool.checkValidationOfGivenEmail(email)) {
            throw new EmailValidationException("This email is not valid: " + email);
        }
        Optional<Admin> optionalAdmin = adminRepository.findByUsernameOrEmailAndDeleted(username, email, false);

        if (optionalAdmin.isPresent() && !optionalAdmin.get().isDeleted()) {
            throw new AlreadyExistUserException("Account with this userName and/or email: " + username + " was already existed ");
        }
        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(username, "REGISTER_ADMIN_ROLE");
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(addManagementRoleRegisterDto.getEmail());
        admin.setEnabled(true);
        admin.setCredentialsNonExpired(true);
        admin.setAccountNonLocked(true);
        admin.setAccountNonExpired(true);

        HashSet<Permission> permissions = new HashSet<>();
        Permission permission;
        if (addManagementRoleRegisterDto.getApplicationRoles().name().equals(SUPER_ADMIN.name())) {
            permission = permissionRepository.findByPermission(ROLE + SUPER_ADMIN);
            permissions.add(permission);
            admin.setRole("ROLE_SUPER_ADMIN");

        } else {
            List<ApplicationPermission> applicationPermissions;
            if (addManagementRoleRegisterDto.getApplicationRoles().name().equals(ADMIN.name())) {
                permission = permissionRepository.findByPermission(ROLE + ADMIN);
                permissions.add(permission);
                applicationPermissions = appTool.getRoleAndPermissionsOfAdmin();
                admin.setRole("ROLE_ADMIN");
            } else if (addManagementRoleRegisterDto.getApplicationRoles().name().equals(MANAGER.name())) {
                permission = permissionRepository.findByPermission(ROLE + MANAGER);
                permissions.add(permission);
                applicationPermissions = appTool.getRoleAndPermissionsOfManager();
                admin.setRole("ROLE_MANAGER");
            } else {
                throw new ValidationDataException("The given Role is not Exist: There are SUPER_ADMIN,MANAGER,ADMIN");
            }

            for (ApplicationPermission applicationPermission : applicationPermissions) {
                permission = permissionRepository.findByPermission(applicationPermission.getName());
                permissions.add(permission);
            }
        }
        admin.setPermissions(permissions);
        admin.setCreatedId(createdAdminId);
        adminRepository.save(admin);
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoric.setResponseBody("Add Admin Role");
        taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);

        adminResponseDto.setUsername(username);
        adminResponseDto.setId(admin.getAdminId());
        adminResponseDto.setUsernameOfAdmin(usernameAdminOfCreated);
        adminResponseDto.setDescription("The " + addManagementRoleRegisterDto.getApplicationRoles().name() + " : " + username + " is registered in successfully: ");
        return adminResponseDto;
    }

    public LoginResponseDto login(UserLoginDto userLoginDto) {
        if (Strings.isNullOrEmpty(userLoginDto.getPassword()) || Strings.isNullOrEmpty(userLoginDto.getUsername())) {
            throw new ValidationDataException("To login you have to give username and password ");
        }
        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(userLoginDto.getUsername(), "LOGIN");
        try {
            String userNameAdmin = userLoginDto.getUsername() + "ADMIN";
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userNameAdmin, userLoginDto.getPassword());

            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            LoginResponseDto loginResponseDto  = jwtGenerator.generateAccessAndRefreshJwtToken(true, authentication,
                    ((ApplicationUserDetails) authentication.getPrincipal()).getId(), userLoginDto.getUsername());

            taskManagerAdminHistoric.setResponseBody("Successful created jwt");
            taskManagerAdminHistoric.setSuccessOperation(true);
            taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);
            return loginResponseDto;
        } catch (InternalAuthenticationServiceException userNameNotFoundException) {
            throw new UserNameNotFoundException("Account with this Username is not found!! ");
        } catch (AuthenticationException authenticationException) {
            taskManagerAdminHistoric.setErrorMessage(authenticationException.getMessage());
            appTool.checkCountLockedAdmin(taskManagerAdminHistoric);
            taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);
            throw new BadCredentialsException("Password not correct ");
        } catch (UserLockoutException userLockoutException) {
            throw new UserLockoutException(userLockoutException.getMessage());
        } catch (Exception e) {
            taskManagerAdminHistoric.setErrorMessage(e.getMessage());
            throw new GlobalException(e.getMessage());
        }

    }
}
