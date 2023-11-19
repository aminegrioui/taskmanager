package com.aminejava.taskmanager.controller.admin;

import com.aminejava.taskmanager.dto.user.ChangePasswordRequestDto;
import com.aminejava.taskmanager.dto.userdetails.UserDetailsDto;
import com.aminejava.taskmanager.services.adminmamagment.SuperAdminProfileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/taskmanager/v1/admin/profile")
public class AdminProfileController {

    private final SuperAdminProfileService adminProfileService;


    public AdminProfileController(SuperAdminProfileService superAdminProfileService) {
        this.adminProfileService = superAdminProfileService;
    }
    @GetMapping
    public ResponseEntity<?> showProfileAdmin(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminProfileService.showProfileOAdmin(requestHeader));
    }
    @PutMapping("/changeUsername/{username}")
    public ResponseEntity<?> changeUsernameOfAdmin(@PathVariable String username, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminProfileService.changeUsernameOfAdmin(username, requestHeader));
    }
    @PutMapping("/changePassword")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordRequestDto changePasswordRequestDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminProfileService.updatePassword(changePasswordRequestDto, requestHeader));
    }
    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        return adminProfileService.logout(request, response);
    }

    @PutMapping("/updateAdminDetails")
    public ResponseEntity<?> updateAdminDetails(@RequestBody UserDetailsDto userDetailsDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminProfileService.updateAdminDetails(userDetailsDto, requestHeader));
    }
    @PutMapping("/uploadPhoto")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile multipartFile, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminProfileService.uploadPhoto(multipartFile, requestHeader));
    }

    @PutMapping("/changeEmail/{newEmail}")
    public ResponseEntity<?> changeEmail(@PathVariable String newEmail, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminProfileService.changeEmail(newEmail, requestHeader));
    }

    @DeleteMapping("/deleteProfilePhoto")
    public ResponseEntity<?> deletePhotoOfAdmin(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminProfileService.deletePhoto(requestHeader));
    }

}
