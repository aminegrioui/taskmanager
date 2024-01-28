package com.aminejava.taskmanager.controller.superadmin;

import com.aminejava.taskmanager.dto.management.*;
import com.aminejava.taskmanager.dto.management.admin.AddUserDto;
import com.aminejava.taskmanager.dto.management.superadmin.AffectPermissionRequestDto;
import com.aminejava.taskmanager.dto.management.superadmin.AffectRoleRequestDto;
import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles;
import com.aminejava.taskmanager.services.adminmamagment.AdminManagementService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PostMapping("/addManagementRole/superadmin")
    @PreAuthorize("hasAnyAuthority('write:super_admin')")
    public ResponseEntity<?> registerSuperAdminManagementRole(@RequestBody AddManagementRoleRegisterDto superAdminRegisterDto, @RequestHeader HttpHeaders requestHeader) {
        superAdminRegisterDto.setApplicationRoles(ApplicationRoles.SUPER_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.registerManagerRole(superAdminRegisterDto, requestHeader));
    }
    @PostMapping("/addManagementRole/admin")
    @PreAuthorize("hasAnyAuthority('write:admin','write:super_admin')")
    public ResponseEntity<?> registerAdminManagementRole(@RequestBody AddManagementRoleRegisterDto adminRegisterDto, @RequestHeader HttpHeaders requestHeader) {
        adminRegisterDto.setApplicationRoles(ApplicationRoles.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.registerManagerRole(adminRegisterDto, requestHeader));
    }
    @PostMapping("/addManagementRole/manager")
    @PreAuthorize("hasAnyAuthority('write:manager','write:admin','write:super_admin')")
    public ResponseEntity<?> registerManagerManagementRole(@RequestBody AddManagementRoleRegisterDto managerRegisterDto, @RequestHeader HttpHeaders requestHeader) {
        managerRegisterDto.setApplicationRoles(ApplicationRoles.MANAGER);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.registerManagerRole(managerRegisterDto, requestHeader));
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

    @PostMapping("/disableUser")
    @PreAuthorize("hasAuthority('disable:user')")
    public ResponseEntity<?> disableUser(@RequestBody ToggleUser toggleUser, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.disableUser(toggleUser, requestHeader));
    }

    @DeleteMapping("/deleteUser/{idUser}")
    @PreAuthorize("hasAuthority('write:user')")
    public ResponseEntity<?> deleteUser(@PathVariable Long idUser, @RequestHeader HttpHeaders httpHeaders) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.deleteUser(idUser, httpHeaders));
    }

    @DeleteMapping("/deleteManagerRole/{idManager}")
    @PreAuthorize("hasAnyAuthority('write:manager','write:admin')")
    public ResponseEntity<?> deleteManager(@PathVariable Long idManager, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.deleteManagerRole(idManager, requestHeader));
    }

    @DeleteMapping("/deleteSuperAdmin/{idSuperAmin}")
    @PreAuthorize("hasAuthority('write:super_admin')")
    public ResponseEntity<?> deleteSupeAdmin(@PathVariable Long idSuperAmin, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(adminManagementService.deleteSuperAdmin(idSuperAmin, requestHeader));
    }

    @PostMapping("/affectManagerRole")
    @PreAuthorize("hasAuthority('affect:role_permission')")
    public ResponseEntity<?> affectRoleToAnAdmin(@RequestBody AffectRoleRequestDto affectRoleRequestDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.affectManagerRole(affectRoleRequestDto, requestHeader));
    }

    @PostMapping("/affectPermission")
    @PreAuthorize("hasAuthority('affect:role_permission')")
    public ResponseEntity<?> affectPermissions(@RequestBody AffectPermissionRequestDto affectPermissionRequestDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.affectPermissions(affectPermissionRequestDto, requestHeader));
    }
    @PostMapping("/saveUser")
    @PreAuthorize("hasAuthority('write:user')")
    public ResponseEntity<?> saveUser(@RequestBody AddUserDto addUserDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagementService.saveUser(addUserDto, requestHeader));
    }
}
