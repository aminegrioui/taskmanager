package com.aminejava.taskmanager.services.adminmamagment;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.management.superadmin.AdminProfileResponseDto;
import com.aminejava.taskmanager.dto.user.ChangePasswordRequestDto;
import com.aminejava.taskmanager.dto.userdetails.UserDetailsDto;
import com.aminejava.taskmanager.exception.GlobalException;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.exception.user.AuthException;
import com.aminejava.taskmanager.exception.user.ChangePasswordException;
import com.aminejava.taskmanager.exception.user.EmailValidationException;
import com.aminejava.taskmanager.model.UserDetails;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.TaskManagerAdminLoggerRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtTool;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.aminejava.taskmanager.system.entities.TaskManagerAdminHistoric;
import com.aminejava.taskmanager.system.exception.BlacklistException;
import com.aminejava.taskmanager.system.services.SystemTaskManager;
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

@Service
public class SuperAdminProfileService {

    @Value("${pathFolder}")
    private String pathFolder;

    private final JwtTool jwtTool;
    private final SystemTaskManager systemTaskManager;
    private final AdminRepository adminRepository;
    private final AppTool appTool;
    private final PasswordEncoder passwordEncoder;
    private final TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository;

    public SuperAdminProfileService(JwtTool jwtGenerator, SystemTaskManager systemTaskManager,
                                    AdminRepository adminRepository, AppTool appTool,
                                    PasswordEncoder passwordEncoder,
                                    TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository) {
        this.jwtTool = jwtGenerator;
        this.systemTaskManager = systemTaskManager;
        this.adminRepository = adminRepository;
        this.appTool = appTool;
        this.passwordEncoder = passwordEncoder;
        this.taskManagerAdminHistoricRepository = taskManagerAdminHistoricRepository;
    }

    public AdminProfileResponseDto showProfileOAdmin(HttpHeaders requestHeader) throws BlacklistException, AuthException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());

        AdminProfileResponseDto adminProfileResponseDto = new AdminProfileResponseDto();


        Admin admin = optionalAdmin.get();
        adminProfileResponseDto.setUsername(admin.getUsername());
        adminProfileResponseDto.setEmail(admin.getEmail());


        UserDetails userDetails = admin.getUserDetails();
        if (userDetails != null) {
            UserDetailsDto userDetailsDto = new UserDetailsDto();
            userDetailsDto.setAddress(userDetails.getAddress());
            userDetailsDto.setLand(userDetails.getLand());
            userDetailsDto.setBirthday(userDetails.getBirthday());
            userDetailsDto.setLastName(userDetails.getLastName());
            userDetailsDto.setFirstName(userDetails.getFirstName());
            userDetailsDto.setDescription(userDetails.getDescription());
            userDetailsDto.setProfilePhoto(userDetails.getImagePath());
            adminProfileResponseDto.setUserDetailsDto(userDetailsDto);
        }

        // Add Historic
        String label = null;
        if (parseTokenResponse.isAdmin()) {
            label = "ADMIN_PROFILE";
        }
        if (parseTokenResponse.isSuperAdmin()) {
            label = "SUPER_ADMIN_PROFILE";
        }
        if (parseTokenResponse.isManager()) {
            label = "MANAGER_PROFILE";
        }
        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), label);
        taskManagerAdminHistoric.setResponseBody(label);
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);

        return adminProfileResponseDto;
    }

    // Update Username

    @Transactional
    public String changeUsernameOfAdmin(String newUsername, HttpHeaders requestHeader) throws BlacklistException {

        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);
        if (!appTool.validUserName(newUsername)) {
            throw new ValidationDataException("The given username is not valid. It must have at minimum 8 character !!");
        }


        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());
        optionalAdmin.get().setUsername(newUsername);

        // Add Historic
        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "CHANGE_USERNAME");
        taskManagerAdminHistoric.setResponseBody("CHANGE_USERNAME");
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);
        return newUsername;
    }
    // Update Password

    public String updatePassword(ChangePasswordRequestDto changePasswordRequestDto, HttpHeaders requestHeader) throws BlacklistException {

        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "CHANGE_PASSWORD");
        if (Strings.isNullOrEmpty(changePasswordRequestDto.getNewPassword()) || Strings.isNullOrEmpty(changePasswordRequestDto.getOldPassword())) {
            throw new ValidationDataException("The given passwords must have a value");
        }
        if (changePasswordRequestDto.getNewPassword().equalsIgnoreCase(changePasswordRequestDto.getOldPassword())) {
            throw new ValidationDataException("The both given passwords are equal. The Both  must be different from each other ");
        }
        if (!appTool.validUserPassword(changePasswordRequestDto.getNewPassword())) {
            throw new ValidationDataException("The given password is not valid. It must have at minimum 10 alphanumeric  !!");
        }

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());

        Admin admin = optionalAdmin.get();

        if (passwordEncoder.matches(changePasswordRequestDto.getOldPassword(), optionalAdmin.get().getPassword())) {
            admin.setPassword(passwordEncoder.encode(changePasswordRequestDto.getNewPassword()));
            taskManagerAdminHistoric.setResponseBody("Password was changed succesfully");
            taskManagerAdminHistoric.setSuccessOperation(true);
            taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);
            // Add the actual token to the blackList
            List<String> headerValues = requestHeader.get("Authorization");
            String token = headerValues.get(0).replace("Bearer", "");
            systemTaskManager.saveNewTokenToBlackList(token);
            return "Password was changed succesfully";
        }

        // Add Historic
        taskManagerAdminHistoric.setErrorMessage("You have given a wrong old password ");
        taskManagerAdminHistoric.setSuccessOperation(false);
        appTool.checkCountLockedAdmin(taskManagerAdminHistoric);
        taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);
        throw new ChangePasswordException("You have given a wrong old password  ");
    }

    public String logout(HttpServletRequest request, HttpServletResponse response) throws BlacklistException {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), null, request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        // Add the actual token to the blackList
        String token = request.getHeader("Authorization").replace("Bearer", "");
        systemTaskManager.saveNewTokenToBlackList(token);

        // Add Historic
        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "ADMIN_LOG_OUT");
        taskManagerAdminHistoric.setResponseBody("Log out Admin");
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);
        return "You have been logged out.";
    }

    // Add/Update UserDetails
    public UserDetailsDto updateAdminDetails(UserDetailsDto userDetailsDto, HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());
        Admin admin = optionalAdmin.get();
        UserDetails userDetails = admin.getUserDetails();
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
        admin.setUserDetails(userDetails);
        adminRepository.save(admin);

        // Save Api in Historic
        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "UPDATE_ADMIN_DETAILS");
        taskManagerAdminHistoric.setResponseBody("Update UserDetails of Admin");
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);

        return userDetailsDto;

    }

    public String uploadPhoto(MultipartFile multipartFile, HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ValidationDataException("You need to give a path photo to upload it ");
        }
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);


        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());
        Admin admin = optionalAdmin.get();
        UserDetails userDetails = admin.getUserDetails();
        try {
            String nameOfImage = multipartFile.getOriginalFilename();


            ClassPathResource resource = new ClassPathResource(pathFolder + nameOfImage);
            String filePath = resource.getPath();
            saveFile(filePath, multipartFile);

            if (userDetails == null) {
                userDetails = new UserDetails();
            }
            userDetails.setImagePath(filePath);
            admin.setUserDetails(userDetails);
            adminRepository.save(admin);

            // Save Api in Historic
            TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "UPLOAD_PHOTO");
            taskManagerAdminHistoric.setResponseBody("Upload Photo of Admin");
            taskManagerAdminHistoric.setSuccessOperation(true);
            taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);

            return filePath;
        } catch (IOException ioException) {
            throw new GlobalException(ioException.toString());
        }

    }

    // Change Email

    @Transactional
    public String changeEmail(String newEmail, HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());
        // Check if the email is correct
        if (!appTool.checkValidationOfGivenEmail(newEmail)) {
            throw new EmailValidationException("This email is not valid: " + newEmail);
        }
        if (optionalAdmin.get().getEmail().equalsIgnoreCase(newEmail)) {
            throw new ValidationDataException("This email is already existed");
        }
        optionalAdmin.get().setEmail(newEmail);

        // Save Api in Historic
        TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "UPDATE_EMAIL");
        taskManagerAdminHistoric.setResponseBody("Update email of Admin");
        taskManagerAdminHistoric.setSuccessOperation(true);
        taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);

        return newEmail;

    }

    public String deletePhoto(HttpHeaders requestHeader) throws BlacklistException {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());
        Admin admin = optionalAdmin.get();
        UserDetails userDetails = admin.getUserDetails();
        if (userDetails == null) {
            throw new ValidationDataException("You have any details in your Profile including the photo !!");
        }
        String pathOfPhoto = userDetails.getImagePath();
        userDetails.setImagePath(null);
        admin.setUserDetails(userDetails);
        adminRepository.save(admin);

        try {
            Files.delete(Paths.get(pathOfPhoto));
            // Save Api in Historic
            TaskManagerAdminHistoric taskManagerAdminHistoric = appTool.logOperationOfAdmins(parseTokenResponse.getUsername(), "DELETE_PHOTO");
            taskManagerAdminHistoric.setResponseBody("Delete Photo Admin");
            taskManagerAdminHistoric.setSuccessOperation(true);
            taskManagerAdminHistoricRepository.save(taskManagerAdminHistoric);
        } catch (IOException e) {
            throw new GlobalException(e.getMessage());
        }

        return "Photo deleted";
    }

    @Transactional
    public Boolean deleteSuperAdminAccount(HttpHeaders requestHeader) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);
        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());
        if(optionalAdmin.isEmpty()){
          throw new AuthException("The Super Admin is not found in DB");
        }
        Admin admin = optionalAdmin.get();
        if(admin.isDeleted()){
            throw new AuthException("The Super Admin is already deleted");
        }

        admin.setDeleted(true);
        return true;
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
