package com.aminejava.taskmanager.system.services;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.TaskManagerAdminLoggerRepository;
import com.aminejava.taskmanager.repository.TaskManagerUserLoggerRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.services.user.UserService;
import com.aminejava.taskmanager.system.entities.TaskManagerAdminHistoric;
import com.aminejava.taskmanager.system.entities.TaskManagerUserHistoric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScanTaskManagerApp {
    private final TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository;
    private final AdminRepository adminRepository;
    private final UserService userService;
    private final AppTool appTool;
    private final UserRepository userRepository;
    private final TaskManagerUserLoggerRepository taskManagerUserLoggerRepository;

    public ScanTaskManagerApp(UserService userService, AppTool appTool,
                              UserRepository userRepository,
                              TaskManagerUserLoggerRepository taskManagerUserLoggerRepository,
                              AdminRepository adminRepository,
                              TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository) {
        this.userService = userService;
        this.appTool = appTool;
        this.userRepository = userRepository;
        this.taskManagerUserLoggerRepository = taskManagerUserLoggerRepository;
        this.adminRepository = adminRepository;
        this.taskManagerAdminHistoricRepository = taskManagerAdminHistoricRepository;
    }

    @Scheduled(cron = "0 0/10 * * * ?", zone = "Europe/Berlin")
    @Transactional
    public void scanLockedUsers() {
        log.info("START_SCAN_USER");
        List<User> userList = userService.getAllUsers().stream().filter(user -> !user.isAccountNonLocked()).collect(Collectors.toList());
        for (User user : userList) {
            ZonedDateTime nowBerlin = appTool.nowTime();
            ZonedDateTime zonedDateTimeOfLockedUser = user.getZonedDateTimeLockedUser();
            if (zonedDateTimeOfLockedUser == null) {
                continue;
            }
            log.info("NOW_BERLIN" + nowBerlin.toString());
            log.info("TIME_LOCKED_USER" + zonedDateTimeOfLockedUser);
            boolean isTime = Duration.between(zonedDateTimeOfLockedUser, nowBerlin).toMinutes() >= 10;
            log.info("From Locked Time: " + zonedDateTimeOfLockedUser + " to now: " + nowBerlin + " is more than 10 Minutes is: " + isTime);
            if (isTime) {
                // Send email to user
                TaskManagerUserHistoric taskManagerUserHistoric = appTool.logOperationOfUsers(user.getUsername(), "SCAN_LOCKED_USERS");
                taskManagerUserHistoric.setSuccessOperation(true);
                taskManagerUserHistoric.setResponseBody("Account with username: " + user.getUsername() + " is again active ");
                taskManagerUserLoggerRepository.save(taskManagerUserHistoric);
                user.setAccountNonLocked(true);
                user.setZonedDateTimeLockedUser(null);
                userRepository.save(user);
                log.info("Account with username: " + user.getUsername() + " is again active ");
            }
        }
        log.info("END_SCAN_USER");
    }

    @Scheduled(cron = "0 0/15 * * * ?", zone = "Europe/Berlin")
    @Transactional
    public void scanLockedAdmin() {
        log.info("START_SCAN_ADMIN");
        List<Admin> adminList = adminRepository.findAll().stream().filter(admin -> !admin.isAccountNonLocked()).collect(Collectors.toList());
        for (Admin admin : adminList) {
            ZonedDateTime nowBerlin = appTool.nowTime();
            ZonedDateTime zonedDateTimeOfLockedUser = admin.getZonedDateTimeLockedUser();
            if (zonedDateTimeOfLockedUser == null) {
                continue;
            }
            log.info("NOW_BERLIN" + nowBerlin.toString());
            log.info("TIME_LOCKED_ADMIN" + zonedDateTimeOfLockedUser);
            boolean isTime = Duration.between(zonedDateTimeOfLockedUser, nowBerlin).toMinutes() >= 10;
            log.info("From Locked Time: " + zonedDateTimeOfLockedUser + " to now: " + nowBerlin + " is more than 10 Minutes is: " + isTime);
            if (isTime) {
                // Send email to user
                TaskManagerAdminHistoric taskManagerUserHistoric = appTool.logOperationOfAdmins(admin.getUsername(), "SCAN_LOCKED_USERS");
                taskManagerUserHistoric.setSuccessOperation(true);
                taskManagerUserHistoric.setResponseBody("Account with username: " + admin.getUsername() + " is again active ");
                taskManagerAdminHistoricRepository.save(taskManagerUserHistoric);
                admin.setAccountNonLocked(true);
                admin.setZonedDateTimeLockedUser(null);
                adminRepository.save(admin);
                log.info("Account with username: " + admin.getUsername() + " is again active ");
            }
        }
        log.info("END_SCAN_ADMIN");
    }


}
