package com.aminejava.taskmanager.controller.superadmin;

import com.aminejava.taskmanager.dto.user.ChangePasswordRequestDto;
import com.aminejava.taskmanager.dto.userdetails.UserDetailsDto;
import com.aminejava.taskmanager.services.adminmamagment.SuperAdminProfileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/taskmanager/v1/superadmin/profile")
public class SuperAdminProfileController {
    private final SuperAdminProfileService superAdminProfileService;

    public SuperAdminProfileController(SuperAdminProfileService superAdminProfileService) {
        this.superAdminProfileService = superAdminProfileService;
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> showProfileOfSuperAdmin(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminProfileService.showProfileOAdmin(requestHeader));
    }

    @PutMapping("/changeUsername/{username}")
    public ResponseEntity<?> changeUsernameOfSuperAdmin(@PathVariable String username, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminProfileService.changeUsernameOfAdmin(username, requestHeader));
    }

    @PutMapping("/changePassword")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordRequestDto changePasswordRequestDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminProfileService.updatePassword(changePasswordRequestDto, requestHeader));
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        return superAdminProfileService.logout(request, response);
    }

    @PutMapping("/updateSuperAdminDetails")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateAdminDetails(@RequestBody UserDetailsDto userDetailsDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminProfileService.updateAdminDetails(userDetailsDto, requestHeader));
    }

    @PutMapping("/uploadPhoto")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile multipartFile, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminProfileService.uploadPhoto(multipartFile, requestHeader));
    }

    @PutMapping("/changeEmail/{newEmail}")
    public ResponseEntity<?> changeEmail(@PathVariable String newEmail, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminProfileService.changeEmail(newEmail, requestHeader));
    }

    @DeleteMapping("/deleteProfilePhoto")
    public ResponseEntity<?> deletePhotoOfSuperAdmin(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminProfileService.deletePhoto(requestHeader));
    }

    @DeleteMapping("/deleteSupperAdminAccount")
    public ResponseEntity<?> deleteUserById(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminProfileService.deleteSuperAdminAccount(requestHeader));
    }


}
