package com.aminejava.taskmanager.services.user;

import com.aminejava.taskmanager.exception.user.AuthException;
import com.aminejava.taskmanager.securityconfig.jwt.JwtTool;
import com.aminejava.taskmanager.system.services.SystemTaskManager;
import com.aminejava.taskmanager.system.entities.TaskManagerUserHistoric;
import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.management.ToggleUser;
import com.aminejava.taskmanager.dto.user.*;
import com.aminejava.taskmanager.dto.userdetails.UserDetailsDto;
import com.aminejava.taskmanager.exception.GlobalException;
import com.aminejava.taskmanager.exception.ResourceNotFoundException;
import com.aminejava.taskmanager.exception.user.ChangePasswordException;
import com.aminejava.taskmanager.exception.user.EmailValidationException;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.model.UserDetails;
import com.aminejava.taskmanager.repository.TaskManagerUserLoggerRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.aminejava.taskmanager.system.exception.BlacklistException;
import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Value("${pathFolder}")
    private String pathFolder;

    private final UserRepository userRepository;
    private final JwtTool jwtTool;
    private final PasswordEncoder passwordEncoder;
    private final AppTool appTool;
    private final TaskManagerUserLoggerRepository taskManagerUserLoggerRepository;
    private final SystemTaskManager systemTaskManager;


    public UserService(UserRepository userRepository, JwtTool jwtTool, PasswordEncoder passwordEncoder, AppTool appTool,
                       TaskManagerUserLoggerRepository taskManagerUserLoggerRepository,
                       SystemTaskManager systemTaskManager) {
        this.userRepository = userRepository;
        this.jwtTool = jwtTool;
        this.passwordEncoder = passwordEncoder;
        this.appTool = appTool;
        this.taskManagerUserLoggerRepository = taskManagerUserLoggerRepository;
        this.systemTaskManager = systemTaskManager;
    }

    // Profile.

    // Show User: Username, email, userDetails

    public UserProfileDto showProfileOfUser(HttpHeaders requestHeader) throws BlacklistException, AuthException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());

        UserProfileDto userProfileDto = new UserProfileDto();


        User user = optionalUser.get();
        userProfileDto.setUsername(user.getUsername());
        userProfileDto.setEmail(user.getEmail());


        UserDetails userDetails = user.getUserDetails();
        if (userDetails != null) {
            UserDetailsDto userDetailsDto = new UserDetailsDto();
            userDetailsDto.setAddress(userDetails.getAddress());
            userDetailsDto.setLand(userDetails.getLand());
            userDetailsDto.setBirthday(userDetails.getBirthday());
            userDetailsDto.setLastName(userDetails.getLastName());
            userDetailsDto.setFirstName(userDetails.getFirstName());
            userDetailsDto.setDescription(userDetails.getDescription());
            userDetailsDto.setProfilePhoto(userDetails.getImagePath());
            userDetailsDto.setMobile(userDetails.getMobile());
            userDetailsDto.setPhone(userDetails.getPhone());
            userDetailsDto.setJob(userDetails.getJob());
            userProfileDto.setUserDetailsDto(userDetailsDto);
        }

        // Add Historic
        TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_PROFILE");
        taskManagerUserHistoric.setResponseBody("Profile of User");
        taskManagerUserHistoric.setSuccessOperation(true);
        taskManagerUserLoggerRepository.save(taskManagerUserHistoric);
        return userProfileDto;

    }
    // Update Username

    @Transactional
    public String changeUsernameOfUser(LoginProfileRequestDto loginProfileRequestDto, HttpHeaders requestHeader) throws BlacklistException {

        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);
        if (!appTool.validUserName(loginProfileRequestDto.getUsername())) {
            throw new ValidationDataException("The given username is not valid. It must have at minimum 8 character !!");
        }


        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());
        optionalUser.get().setUsername(loginProfileRequestDto.getUsername());

        // Add Historic
        TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_CHANGE_USERNAME");
        taskManagerUserHistoric.setResponseBody("Change username of User");
        taskManagerUserHistoric.setSuccessOperation(true);
        taskManagerUserLoggerRepository.save(taskManagerUserHistoric);
        return loginProfileRequestDto.getUsername();

    }

    // Update Password

    public String updatePassword(ChangePasswordRequestDto changePasswordRequestDto, HttpHeaders requestHeader) throws BlacklistException {

        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        TaskManagerUserHistoric taskManagerUserLogger = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "CHANGE_PASSWORD");
        if (Strings.isNullOrEmpty(changePasswordRequestDto.getNewPassword()) || Strings.isNullOrEmpty(changePasswordRequestDto.getOldPassword())) {
            throw new ValidationDataException("The given passwords must have a value");
        }
        if (changePasswordRequestDto.getNewPassword().equalsIgnoreCase(changePasswordRequestDto.getOldPassword())) {
            throw new ValidationDataException("The both given passwords are equal. The Both  must be different from each other ");
        }
        if (!appTool.validUserPassword(changePasswordRequestDto.getNewPassword())) {
            throw new ValidationDataException("The given password is not valid. It must have at minimum 10 alphanumeric  !!");
        }

        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());

        User user = optionalUser.get();

        if (passwordEncoder.matches(changePasswordRequestDto.getOldPassword(), optionalUser.get().getPassword())) {
            user.setPassword(passwordEncoder.encode(changePasswordRequestDto.getNewPassword()));
            taskManagerUserLogger.setResponseBody("Password was changed succesfully");
            taskManagerUserLogger.setSuccessOperation(true);
            taskManagerUserLoggerRepository.save(taskManagerUserLogger);
            // Add the actual token to the blackList
            List<String> headerValues = requestHeader.get("Authorization");
            String token = headerValues.get(0).replace("Bearer", "");
            systemTaskManager.saveNewTokenToBlackList(token);
            return "Password was changed succesfully. Please Login again";
        }

        taskManagerUserLogger.setErrorMessage("You have given a wrong old password ");
        taskManagerUserLogger.setSuccessOperation(false);
        appTool.checkCountLocked(taskManagerUserLogger);
        taskManagerUserLoggerRepository.save(taskManagerUserLogger);
        throw new ChangePasswordException("You have given a wrong old password  ");
    }

    public String logout(HttpServletRequest request, HttpServletResponse response) throws BlacklistException {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), null, request);


        // Add the actual token to the blackList
        String token = request.getHeader("Authorization").replace("Bearer", "");
        systemTaskManager.saveNewTokenToBlackList(token);

        // Add Historic
        TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_LOG_OUT");
        taskManagerUserHistoric.setResponseBody("Log out User");
        taskManagerUserHistoric.setSuccessOperation(true);
        taskManagerUserLoggerRepository.save(taskManagerUserHistoric);
        return "You have been logged out.";
    }


    // Add/Update UserDetails
    public UserDetailsDto updateUserDetails(UserDetailsDto userDetailsDto, HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());
        User user = optionalUser.get();
        UserDetails userDetails = user.getUserDetails();
        if (userDetails == null) {
            userDetails = new UserDetails();
        }

        if (!Strings.isNullOrEmpty(userDetailsDto.getAddress())) {
            userDetails.setAddress(userDetailsDto.getAddress());
        }
        if (!Strings.isNullOrEmpty(userDetailsDto.getLand())) {
            userDetails.setLand(userDetailsDto.getLand());
        }

        if (!Strings.isNullOrEmpty(userDetailsDto.getBirthday())) {
            userDetails.setBirthday(userDetailsDto.getBirthday());
        }

        if (!Strings.isNullOrEmpty(userDetailsDto.getLastName())) {
            userDetails.setLastName(userDetailsDto.getLastName());
        }
        if (!Strings.isNullOrEmpty(userDetailsDto.getFirstName())) {
            userDetails.setFirstName(userDetailsDto.getFirstName());
        }
        if (!Strings.isNullOrEmpty(userDetailsDto.getDescription())) {
            userDetails.setDescription(userDetailsDto.getDescription());
        }
        if (!Strings.isNullOrEmpty(userDetailsDto.getMobile())) {
            userDetails.setMobile(userDetailsDto.getMobile());
        }
        if (!Strings.isNullOrEmpty(userDetailsDto.getPhone())) {
            userDetails.setPhone(userDetailsDto.getPhone());
        }
        if (!Strings.isNullOrEmpty(userDetailsDto.getJob())) {
            userDetails.setJob(userDetailsDto.getJob());
        }
        user.setUserDetails(userDetails);
        userRepository.save(user);

        // Add Historic
        TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_USER_DETAILS");
        taskManagerUserHistoric.setResponseBody("UserDetails of User");
        taskManagerUserHistoric.setSuccessOperation(true);
        taskManagerUserLoggerRepository.save(taskManagerUserHistoric);

        return userDetailsDto;

    }

    public String uploadPhoto(MultipartFile multipartFile, HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ValidationDataException("You need to give a path photo to upload it ");
        }
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);


        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());
        User user = optionalUser.get();
        UserDetails userDetails = user.getUserDetails();
        try {
            String nameOfImage = multipartFile.getOriginalFilename();


            ClassPathResource resource = new ClassPathResource(pathFolder + nameOfImage);
            String filePath = resource.getPath();
            saveFile(filePath, multipartFile);

            if (userDetails == null) {
                userDetails = new UserDetails();
            }
            userDetails.setImagePath(filePath);
            user.setUserDetails(userDetails);
            userRepository.save(user);

            // Add Historic
            TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_UPLOAD_PHOTO");
            taskManagerUserHistoric.setResponseBody("Opload photo of User");
            taskManagerUserHistoric.setSuccessOperation(true);
            taskManagerUserLoggerRepository.save(taskManagerUserHistoric);

            return filePath;
        } catch (IOException ioException) {
            throw new GlobalException(ioException.toString());
        }

    }

    // Change Email

    @Transactional
    public String changeEmail(LoginProfileRequestDto loginProfileRequestDto, HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());
        // Check if the email is correct
        if (!appTool.checkValidationOfGivenEmail(loginProfileRequestDto.getEmail())) {
            throw new EmailValidationException("This email is not valid: " + loginProfileRequestDto.getEmail());
        }
        if (optionalUser.get().getEmail().equalsIgnoreCase(loginProfileRequestDto.getEmail())) {
            throw new ValidationDataException("This email is already existed");
        }
        optionalUser.get().setEmail(loginProfileRequestDto.getEmail());

        // Add Historic
        TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_CHANGE_EMAIL");
        taskManagerUserHistoric.setResponseBody("Change email of User");
        taskManagerUserHistoric.setSuccessOperation(true);
        taskManagerUserLoggerRepository.save(taskManagerUserHistoric);
        return loginProfileRequestDto.getEmail();

    }

    public String deletePhotoOfUser(HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());
        User user = optionalUser.get();
        UserDetails userDetails = user.getUserDetails();
        if (userDetails == null) {
            throw new ValidationDataException("You have any details in your Profile including the photo !!");
        }
        String pathOfPhoto = userDetails.getImagePath();
        userDetails.setImagePath(null);
        user.setUserDetails(userDetails);
        userRepository.save(user);


        try {
            Files.delete(Paths.get(pathOfPhoto));
            // Add Historic
            TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_DELETE_PHOTO");
            taskManagerUserHistoric.setResponseBody("Delete photo of User");
            taskManagerUserHistoric.setSuccessOperation(true);
            taskManagerUserLoggerRepository.save(taskManagerUserHistoric);
        } catch (IOException e) {
            throw new GlobalException(e.getMessage());
        }

        return "Photo deleted";
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream().filter(user -> user.isEnabled() && !user.isDeleted())
                .collect(Collectors.toList());
    }

    public List<UserResponseDtoForGetAll> getAllUsersShorts() {
        return userRepository.findAll().stream().filter(user -> user.isEnabled() && !user.isDeleted())
                .map(user ->
                        new UserResponseDtoForGetAll(user.getUsername(), user.getEmail(),
                                user.getAdmin() != null ? user.getAdmin().getUsername() : null,
                                user.getAdmin() != null ? user.getAdmin().getAdminId() : null))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse disableUser(ToggleUser toggleUser) {
        Optional<User> optionalUser = userRepository.findByUsername(toggleUser.getUsername());
        UserResponse userResponseDto = new UserResponse();

        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("This User with this username: " + toggleUser.getUsername() + " is not found ");
        }

        if (toggleUser.isToggle() == optionalUser.get().isEnabled()) {
            String description = "The User: " + toggleUser.getUsername() + " is already " + (toggleUser.isToggle() ? "enabled" : "disabled");
            userResponseDto.setDescription(description);
            return userResponseDto;
        }
        optionalUser.get().setEnabled(toggleUser.isToggle());
        userResponseDto.setUsername(toggleUser.getUsername());
        String toggle = toggleUser.isToggle() ? " is enable " : "is disabled ";
        userResponseDto.setDescription("The user with username: " + toggleUser.getUsername() + toggle);
        appTool.saveNewBlackListWithUsername(toggleUser.getUsername(),"DISABLE_USER");
        return userResponseDto;
    }

    public DeleteUserResponseDto deleteUser(long id, HttpHeaders requestHeader, String role) throws BlacklistException {
        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<User> optionalUser = userRepository.findUserById(id);


        if (optionalUser.isEmpty() || optionalUser.get().isDeleted()) {
            throw new ResourceNotFoundException("This user with id: " + id + " is not found or  is already deleted");
        }
        if (!Strings.isNullOrEmpty(role) && parseTokenResponse.isAdmin()) {
            if (optionalUser.get().getAdmin() == null || (optionalUser.get().getAdmin().getAdminId().longValue() != parseTokenResponse.getId().longValue())) {
                throw new ValidationDataException("You can not delete this user. you have not created !! ");
            }
        }
        optionalUser.get().setDeleted(true);
        appTool.saveNewBlackListWithUsername(optionalUser.get().getUsername(),"DELETED_USER");
        // Add Historic
        if (Strings.isNullOrEmpty(role)) {
            TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_DELETE_ACCOUNT");
            taskManagerUserHistoric.setResponseBody("Delete user ");
            taskManagerUserHistoric.setSuccessOperation(true);
            taskManagerUserLoggerRepository.save(taskManagerUserHistoric);
        }

        return new DeleteUserResponseDto(true, "This user with id: " + id + " is  deleted");
    }

    private void saveFile(String uploadPath, MultipartFile multipartFile) throws IOException {
        Path path = Paths.get(uploadPath);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Could not save image file: " + ioe);
        }
    }

}
