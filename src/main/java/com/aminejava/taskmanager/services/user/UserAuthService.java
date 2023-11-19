package com.aminejava.taskmanager.services.user;

import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.system.entities.TaskManagerUserHistoric;
import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.user.UserLoginDto;
import com.aminejava.taskmanager.dto.user.UserRegisterDto;
import com.aminejava.taskmanager.dto.user.UserResponseDto;
import com.aminejava.taskmanager.exception.GlobalException;
import com.aminejava.taskmanager.exception.user.*;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.TaskManagerUserLoggerRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.userdeatails.models.ApplicationUserDetails;
import com.google.common.base.Strings;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles.USER;

@Service
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final AdminRepository adminRepository;
    private final AppTool appTool;
    public final TaskManagerUserLoggerRepository taskManagerUserLoggerRepository;

    public UserAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtGenerator jwtGenerator, AdminRepository adminRepository, AppTool appTool, TaskManagerUserLoggerRepository taskManagerUserLoggerRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
        this.adminRepository = adminRepository;
        this.appTool = appTool;
        this.taskManagerUserLoggerRepository = taskManagerUserLoggerRepository;
    }

    @Transactional
    public UserResponseDto registerUser(UserRegisterDto userDto, Long managerId) {
        UserResponseDto userResponseDto = new UserResponseDto();
        if (Strings.isNullOrEmpty(userDto.getEmail()) || Strings.isNullOrEmpty(userDto.getUsername()) || Strings.isNullOrEmpty(userDto.getPassword())) {
            throw new ValidationDataException("To Register a new User you have to give username, email and password ");
        }
        if (!appTool.validUserName(userDto.getUsername())) {
            throw new ValidationDataException("The given username is not valid. It must have at minimum 8 aplha character !!");
        }
        if (!appTool.validUserPassword(userDto.getPassword())) {
            throw new ValidationDataException("The given password is not valid. It must have at minimum 10 alphanumeric  !!");
        }
        if (!appTool.checkValidationOfGivenEmail(userDto.getEmail())) {
            throw new EmailValidationException("This email is not valid: " + userDto.getEmail());
        }
        // Check if the user is valid

        Optional<User> optionalUser = userRepository.findByUsernameOrEmail(userDto.getUsername(), userDto.getEmail());

        if (optionalUser.isEmpty()) {
            TaskManagerUserHistoric taskManagerUserLogger = appTool.logOperationOfUsers(userDto.getUsername(), "REGISTER");
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setEmail(userDto.getEmail());
            user.setEnabled(true);
            user.setCredentialsNonExpired(true);
            user.setAccountNonLocked(true);
            user.setAccountNonExpired(true);
            user.setRole("ROLE_" + USER.name());
            if (managerId != null && managerId > 0) {
                Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(managerId);
                user.setAdmin(optionalAdmin.orElse(null));
            }
            userRepository.save(user);
            userResponseDto.setUserId(user.getId());
            userResponseDto.setUsername(userDto.getUsername());
            userResponseDto.setDescription("User with userName " + userDto.getUsername() + " was created");
            taskManagerUserLogger.setResponseBody("User with userName " + userDto.getUsername() + " was registered");
            taskManagerUserLogger.setSuccessOperation(true);
            taskManagerUserLoggerRepository.save(taskManagerUserLogger);
            return userResponseDto;
        }
        throw new AlreadyExistUserException("User with this userName and email: " + userDto.getUsername() + " was already existed ");
    }

    public String login(UserLoginDto userLoginDto) {
        if (Strings.isNullOrEmpty(userLoginDto.getPassword()) || Strings.isNullOrEmpty(userLoginDto.getUsername())) {
            throw new ValidationDataException("To login you have to give username and password ");
        }
        TaskManagerUserHistoric taskManagerUserLogger = appTool.logOperationOfUsers(userLoginDto.getUsername(), "LOGIN");
        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword());
            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            String jwt = jwtGenerator.generateJwtToken(false, authentication, ((ApplicationUserDetails) authentication.getPrincipal()).getId(), userLoginDto.getUsername());
            taskManagerUserLogger.setResponseBody("Successful jwt");
            taskManagerUserLogger.setSuccessOperation(true);
            taskManagerUserLoggerRepository.save(taskManagerUserLogger);
            return jwt;

        } catch (InternalAuthenticationServiceException userNameNotFoundException) {
            throw new UserNameNotFoundException("Username not found !! ");
        } catch (AuthenticationException authenticationException) {
            taskManagerUserLogger.setErrorMessage(authenticationException.getMessage());
            appTool.checkCountLocked(taskManagerUserLogger);
            taskManagerUserLoggerRepository.save(taskManagerUserLogger);
            throw new BadCredentialsException("Password not correct ");
        } catch (UserLockoutException userLockoutException) {
            throw new UserLockoutException(userLockoutException.getMessage());
        } catch (Exception e) {
            taskManagerUserLogger.setErrorMessage(e.getMessage());
            throw new GlobalException(e.getMessage());
        }

    }

}
