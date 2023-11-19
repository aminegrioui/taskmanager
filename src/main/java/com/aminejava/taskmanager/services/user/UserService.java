package com.aminejava.taskmanager.services.user;

import com.aminejava.taskmanager.exception.user.AuthException;
import com.aminejava.taskmanager.repository.AdminRepository;
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
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.aminejava.taskmanager.system.exception.BlacklistException;
import com.google.common.base.Strings;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final JwtGenerator jwtGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AppTool appTool;
    private final TaskManagerUserLoggerRepository taskManagerUserLoggerRepository;
    private final SystemTaskManager systemTaskManager;
    private final AdminRepository adminRepository;


    public UserService(UserRepository userRepository, JwtGenerator jwtGenerator, PasswordEncoder passwordEncoder, AppTool appTool,
                       TaskManagerUserLoggerRepository taskManagerUserLoggerRepository,
                       SystemTaskManager systemTaskManager,
                       AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.passwordEncoder = passwordEncoder;
        this.appTool = appTool;
        this.taskManagerUserLoggerRepository = taskManagerUserLoggerRepository;
        this.systemTaskManager = systemTaskManager;
        this.adminRepository = adminRepository;
    }

    // Profile.

    // Show User: Username, email, userDetails

    public UserProfileDto showProfileOfUser(HttpHeaders requestHeader) throws BlacklistException, AuthException {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

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
    public String changeUsernameOfUser(String newUsername, HttpHeaders requestHeader) throws BlacklistException {

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);
        if (!appTool.validUserName(newUsername)) {
            throw new ValidationDataException("The given username is not valid. It must have at minimum 8 character !!");
        }


        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());
        optionalUser.get().setUsername(newUsername);

        // Add Historic
        TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_CHANGE_USERNAME");
        taskManagerUserHistoric.setResponseBody("Change username of User");
        taskManagerUserHistoric.setSuccessOperation(true);
        taskManagerUserLoggerRepository.save(taskManagerUserHistoric);
        return newUsername;

    }

    // Update Password

    public String updatePassword(ChangePasswordRequestDto changePasswordRequestDto, HttpHeaders requestHeader) throws BlacklistException {

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        TaskManagerUserHistoric taskManagerUserLogger = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "CHANGE_PASSWORD");
        if (Strings.isNullOrEmpty(changePasswordRequestDto.getNewPassword()) || Strings.isNullOrEmpty(changePasswordRequestDto.getOldUPassword())) {
            throw new ValidationDataException("The given passwords must have a value");
        }
        if (changePasswordRequestDto.getNewPassword().equalsIgnoreCase(changePasswordRequestDto.getOldUPassword())) {
            throw new ValidationDataException("The both given passwords are equal. The Both  must be different from each other ");
        }
        if (!appTool.validUserPassword(changePasswordRequestDto.getNewPassword())) {
            throw new ValidationDataException("The given password is not valid. It must have at minimum 10 alphanumeric  !!");
        }

        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());

        User user = optionalUser.get();

        if (passwordEncoder.matches(changePasswordRequestDto.getOldUPassword(), optionalUser.get().getPassword())) {
            user.setPassword(passwordEncoder.encode(changePasswordRequestDto.getNewPassword()));
            taskManagerUserLogger.setResponseBody("Password was changed succesfully");
            taskManagerUserLogger.setSuccessOperation(true);
            taskManagerUserLoggerRepository.save(taskManagerUserLogger);
            // Add the actual token to the blackList
            List<String> headerValues = requestHeader.get("Authorization");
            String token = headerValues.get(0).replace("Bearer", "");
            systemTaskManager.saveNewTokenToBlackList(token);
            return "Password was changed succesfully";
        }

        taskManagerUserLogger.setErrorMessage("You have given a wrong old password ");
        taskManagerUserLogger.setSuccessOperation(false);
        appTool.checkCountLocked(taskManagerUserLogger);
        taskManagerUserLoggerRepository.save(taskManagerUserLogger);
        throw new ChangePasswordException("You have given a wrong old password  ");
    }

    public String logout(HttpServletRequest request, HttpServletResponse response) throws BlacklistException {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), null, request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

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
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

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
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
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
    public String changeEmail(String newEmail, HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<User> optionalUser = userRepository.findUserById(parseTokenResponse.getId());
        // Check if the email is correct
        if (!appTool.checkValidationOfGivenEmail(newEmail)) {
            throw new EmailValidationException("This email is not valid: " + newEmail);
        }
        if (optionalUser.get().getEmail().equalsIgnoreCase(newEmail)) {
            throw new ValidationDataException("This email is already existed");
        }
        optionalUser.get().setEmail(newEmail);

        // Add Historic
        TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(parseTokenResponse.getUsername(), "USER_CHANGE_EMAIL");
        taskManagerUserHistoric.setResponseBody("Change email of User");
        taskManagerUserHistoric.setSuccessOperation(true);
        taskManagerUserLoggerRepository.save(taskManagerUserHistoric);
        return newEmail;

    }

    public String deletePhotoOfUser(HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

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
                                user.getAdmin() != null ? user.getAdmin().getUsername():null,
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
        return userResponseDto;
    }

    @Transactional
    public DeleteUserResponseDto deleteUser(long id, HttpHeaders requestHeader, String role) throws BlacklistException {
        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<User> optionalUser = userRepository.findUserById(id);


        if (optionalUser.isEmpty() || optionalUser.get().isDeleted()) {
            throw new ResourceNotFoundException("This user with id: " + id + " is not found or  is already deleted");
        }
        if (!Strings.isNullOrEmpty(role) && parseTokenResponse.isAdmin()) {
            if (optionalUser.get().getAdmin()==null || ( optionalUser.get().getAdmin().getAdminId().longValue() != parseTokenResponse.getId().longValue())) {
                throw new ValidationDataException("You can not delete this user. you have not created !! ");
            }
        }
        optionalUser.get().setDeleted(true);
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
