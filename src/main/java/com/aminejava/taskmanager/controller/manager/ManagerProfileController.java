package com.aminejava.taskmanager.controller.manager;

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
@RequestMapping("/taskmanager/v1/manager/profile")
public class ManagerProfileController {

    private final SuperAdminProfileService adminProfileService;


    public ManagerProfileController(SuperAdminProfileService superAdminProfileService) {
        this.adminProfileService = superAdminProfileService;
    }
    @GetMapping
    public ResponseEntity<?> showProfileManager(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminProfileService.showProfileOAdmin(requestHeader));
    }
    @PutMapping("/changeUsername/{username}")
    public ResponseEntity<?> changeUsernameOfManager(@PathVariable String username, @RequestHeader HttpHeaders requestHeader) {
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

    @PutMapping("/updateManagerDetails")
    public ResponseEntity<?> updateManagerDetails(@RequestBody UserDetailsDto userDetailsDto, @RequestHeader HttpHeaders requestHeader) {
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
    public ResponseEntity<?> deletePhotoOfManager(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminProfileService.deletePhoto(requestHeader));
    }

}
