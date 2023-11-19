package com.aminejava.taskmanager.controller.superadmin;

import com.aminejava.taskmanager.dto.management.*;
import com.aminejava.taskmanager.dto.management.admin.AddUserDto;
import com.aminejava.taskmanager.dto.management.superadmin.AffectPermissionRequestDto;
import com.aminejava.taskmanager.dto.management.superadmin.AffectRoleRequestDto;
import com.aminejava.taskmanager.services.adminmamagment.AdminManagementService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/taskmanager/v1/superadmin/management")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;


    public AdminManagementController(AdminManagementService adminManagementService) {
        this.adminManagementService = adminManagementService;
    }

    /**
     * Register superAdmin, Admin or Manager
     *
     * @param superAdminRegisterDto
     * @param requestHeader
     * @return
     */
    @PostMapping("/addManagementRole")
    public ResponseEntity<?> registerManagementRole(@RequestBody AddManagementRoleRegisterDto superAdminRegisterDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.registerManagerRole(superAdminRegisterDto, requestHeader));
    }


    @GetMapping("/getAllProjects")
    public ResponseEntity<?> getAllProjects(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.getAllProjects(requestHeader));
    }

    @GetMapping("/getAllUsers/{isAll}")
    public ResponseEntity<?> getAllUsers(@PathVariable(required = false) boolean isAll,@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.getAllUsers(isAll,requestHeader));
    }

    @GetMapping("/getAllUsersWithDetails/{isAll}")
    public ResponseEntity<?> getAllUsersWithDetails(@PathVariable(required = false) boolean isAll,@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.getAllUsersWithDetails(isAll,requestHeader));
    }

    @GetMapping("/getAllTasks")
    public ResponseEntity<?> getAllTasks(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.getAllTasks(requestHeader));
    }

    @GetMapping("/getAllSubTasks")
    public ResponseEntity<?> getAllSubTasks(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.getAllSubTasks(requestHeader));
    }

    @GetMapping("/getAllManagersRole")
    public ResponseEntity<?> getAllManagersRole(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.getAllManagersRole(requestHeader));
    }

    // chameg name
    @PostMapping("/disableUser")
    public ResponseEntity<?> disableUser(@RequestBody ToggleUser toggleUser, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.disableUser(toggleUser, requestHeader));
    }

    @DeleteMapping("/deleteUser/{idUser}")
    public ResponseEntity<?> deleteUser(@PathVariable Long idUser, @RequestHeader HttpHeaders httpHeaders) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.deleteUser(idUser, httpHeaders));
    }

    @DeleteMapping("/deleteManagerRole/{idManager}")
    public ResponseEntity<?> deleteManager(@PathVariable Long idManager, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.deleteManagerRole(idManager, requestHeader));
    }

    @DeleteMapping("/deleteSuperAdmin/{idSuperAmin}")
    public ResponseEntity<?> deleteSupeAdmin(@PathVariable Long idSuperAmin, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.deleteSuperAdmin(idSuperAmin, requestHeader));
    }

    @PostMapping("/affectManagerRole")
    public ResponseEntity<?> affectRoleToAnAdmin(@RequestBody AffectRoleRequestDto affectRoleRequestDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.affectManagerRole(affectRoleRequestDto, requestHeader));
    }

    @PostMapping("/affectPermission")
    public ResponseEntity<?> affectPermissions(@RequestBody AffectPermissionRequestDto affectPermissionRequestDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.affectPermissions(affectPermissionRequestDto, requestHeader));
    }
    @PostMapping("/saveUser")
    public ResponseEntity<?> saveUser(@RequestBody AddUserDto addUserDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.saveUser(addUserDto, requestHeader));
    }
}
