package com.aminejava.taskmanager.controller.user;

import com.aminejava.taskmanager.dto.user.ChangePasswordRequestDto;
import com.aminejava.taskmanager.dto.userdetails.UserDetailsDto;
import com.aminejava.taskmanager.services.user.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/taskmanager/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> showProfileOfUser(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.showProfileOfUser(requestHeader));
    }

    @PutMapping("/changeUsername/{username}")
    public ResponseEntity<?> changeUsernameOfUser(@PathVariable String username, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.changeUsernameOfUser(username, requestHeader));
    }

    @PutMapping("/changePassword")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordRequestDto changePasswordRequestDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updatePassword(changePasswordRequestDto, requestHeader));
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        return userService.logout(request, response);
    }

    @PutMapping("/updateUserDetails")
    public ResponseEntity<?> updateUserDetails(@RequestBody UserDetailsDto userDetailsDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserDetails(userDetailsDto, requestHeader));
    }

    @PutMapping("/uploadPhoto")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile multipartFile, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.uploadPhoto(multipartFile, requestHeader));
    }

    @PutMapping("/changeEmail/{newEmail}")
    public ResponseEntity<?> changeEmail(@PathVariable String newEmail, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.changeEmail(newEmail, requestHeader));
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long userId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.deleteUser(userId, requestHeader, null));
    }

    @DeleteMapping("/deleteProfilePhoto")
    public ResponseEntity<?> deletePhotoOfUser(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.deletePhotoOfUser(requestHeader));
    }

}
