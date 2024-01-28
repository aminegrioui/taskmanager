package com.aminejava.taskmanager.controller.admin;

import com.aminejava.taskmanager.dto.management.AddManagementRoleRegisterDto;
import com.aminejava.taskmanager.dto.management.admin.AddUserDto;
import com.aminejava.taskmanager.services.adminmamagment.AdminManagementService;
import com.aminejava.taskmanager.services.adminmamagment.AdminService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/taskmanager/v1/admin/management")
public class AdminController {

    private final AdminService adminService;
    private final AdminManagementService adminManagementService;

    public AdminController(AdminService adminService, AdminManagementService adminManagementService) {
        this.adminService = adminService;
        this.adminManagementService = adminManagementService;
    }

    @GetMapping("/allMangers")
    public ResponseEntity<?> getAllManagers(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body( adminService.getAllCreatedManagers(requestHeader));
    }

    @GetMapping("/allUsers/{isAll}")
    public ResponseEntity<?> getAllUsers(@PathVariable(required = false) boolean isAll,@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.getAllUsers(isAll,requestHeader));
    }
    @GetMapping("/getAllUsersWithDetails/{isAll}")
    public ResponseEntity<?> getAllUsersWithDetails(@PathVariable(required = false) boolean isAll,@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.getAllUsersWithDetails(isAll,requestHeader));
    }

    @PostMapping("/addManager")
    @PreAuthorize("hasAuthority('write:manager')")
    public ResponseEntity<?> addManager(@RequestBody AddManagementRoleRegisterDto addManagementRoleRegisterDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.addManager(addManagementRoleRegisterDto, requestHeader));
    }

    @DeleteMapping("/deleteManager/{id}")
    @PreAuthorize("hasAuthority('write:manager')")
    public ResponseEntity<?> deleteManager(@PathVariable Long id, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.deleteManager(id, requestHeader));
    }
    @DeleteMapping("/deleteUser/{id}")
    @PreAuthorize("hasAuthority('write:user')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.deleteUser(id, requestHeader));
    }

    @PostMapping("/addUser")
    @PreAuthorize("hasAuthority('write:user')")
    public ResponseEntity<?> saveUser(@RequestBody AddUserDto addUserDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.saveUser(addUserDto, requestHeader));
    }
}
