package com.aminejava.taskmanager.services.user;

import com.aminejava.taskmanager.dto.user.*;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.system.dto.EmailResponse;
import com.aminejava.taskmanager.system.entities.TaskManagerUserHistoric;
import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.exception.GlobalException;
import com.aminejava.taskmanager.exception.user.*;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.TaskManagerUserLoggerRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.userdeatails.models.ApplicationUserDetails;
import com.aminejava.taskmanager.system.services.EmailService;
import com.aminejava.taskmanager.system.services.EncryptionService;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.path}")
    private String link;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final AdminRepository adminRepository;
    private final AppTool appTool;
    public final TaskManagerUserLoggerRepository taskManagerUserLoggerRepository;
    private final EmailService emailService;
    private final EncryptionService encryptionService;

    public UserAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtGenerator jwtGenerator, AdminRepository adminRepository, AppTool appTool, TaskManagerUserLoggerRepository taskManagerUserLoggerRepository, EmailService emailService, EncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
        this.adminRepository = adminRepository;
        this.appTool = appTool;
        this.taskManagerUserLoggerRepository = taskManagerUserLoggerRepository;
        this.emailService = emailService;
        this.encryptionService = encryptionService;
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

        Optional<User> optionalUserWithUser = userRepository.findByUsername(userDto.getUsername());
        Optional<User> optionalUserWithEmail = userRepository.findByEmail(userDto.getEmail());

        if (optionalUserWithUser.isEmpty() && optionalUserWithEmail.isEmpty()) {
            TaskManagerUserHistoric taskManagerUserLogger = appTool.logOperationOfUsers(userDto.getUsername(), "REGISTER");
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            emailService.sendEmailToUser(userDto.getEmail(), user.getUsername(), link);
            user.setEmail(userDto.getEmail());
            user.setEnabled(false);
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
        throw new AlreadyExistUserException("User with this userName " + userDto.getUsername() + " and email: " + userDto.getUsername() + " was already existed ");
    }

    public LoginResponseDto login(UserLoginDto userLoginDto) {
        if (Strings.isNullOrEmpty(userLoginDto.getPassword()) || Strings.isNullOrEmpty(userLoginDto.getUsername())) {
            throw new ValidationDataException("To login you have to give username and password ");
        }
        TaskManagerUserHistoric taskManagerUserLogger = appTool.logOperationOfUsers(userLoginDto.getUsername(), "LOGIN");
        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword());
            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            LoginResponseDto loginResponseDto = jwtGenerator.generateAccessAndRefreshJwtToken(false, authentication, ((ApplicationUserDetails) authentication.getPrincipal()).getId(), userLoginDto.getUsername());
            taskManagerUserLogger.setResponseBody("Successful jwt");
            taskManagerUserLogger.setSuccessOperation(true);
            taskManagerUserLoggerRepository.save(taskManagerUserLogger);
            return loginResponseDto;

        } catch (InternalAuthenticationServiceException userNameNotFoundException) {
            throw new UserNameNotFoundException(userNameNotFoundException.getMessage());
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

    @Transactional
    public String responseOfValidationEmailLink(String encryptData) {
        EmailResponse emailResponse = encryptionService.decryptUserData(encryptData);
        if (emailResponse.isActive()) {
            Optional<User> optionalUser = userRepository.findByUsernameOrEmail(emailResponse.getUsername(), emailResponse.getEmail());
            User user = optionalUser.get();
            if(user.isEnabled()){
                return "The user: "+emailResponse.getUsername()+" is already enabled";
            }
            if(emailResponse.isActive()){
                user.setEnabled(true);
                return "The User " + user.getUsername() + " is now enable";
            }

        }
        emailService.sendEmailToUser(emailResponse.getEmail(), emailResponse.getUsername(),link);
        return emailResponse.getMessage();
    }

}
