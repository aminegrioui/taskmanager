package com.aminejava.taskmanager.controller.tool;


import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.*;
import com.aminejava.taskmanager.system.entities.BlacklistEntry;
import com.aminejava.taskmanager.system.entities.TaskManagerAdminHistoric;
import com.aminejava.taskmanager.system.entities.TaskManagerUserHistoric;
import com.aminejava.taskmanager.exception.user.UserLockoutException;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationPermission;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationPermission.*;

@Service
public class AppTool {

    public final TaskManagerUserLoggerRepository taskManagerUserLoggerRepository;
    private final UserRepository userRepository;
    private final TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository;
    private final AdminRepository adminRepository;
    private final BlacklistEntryRepository blacklistEntryRepository;

    public AppTool(
            TaskManagerUserLoggerRepository taskManagerUserLoggerRepository, UserRepository userRepository,
            TaskManagerAdminLoggerRepository taskManagerAdminHistoricRepository, AdminRepository adminRepository, BlacklistEntryRepository blacklistEntryRepository) {
        this.taskManagerUserLoggerRepository = taskManagerUserLoggerRepository;
        this.userRepository = userRepository;
        this.taskManagerAdminHistoricRepository = taskManagerAdminHistoricRepository;
        this.adminRepository = adminRepository;
        this.blacklistEntryRepository = blacklistEntryRepository;
    }

    public List<ApplicationPermission> getAllApplicationPermissions() {
        return Arrays.asList(
                WRITE_USER,

                READ_USER,

                READ_MANAGER,

                DISABLE_USER,

                READ_ADMIN,

                WRITE_ADMIN,

                WRITE_MANAGER,

                READ_MANAGER,

                WRITE_PROJECT,

                READ_PROJECT,

                WRITE_SUBTASK,

                READ_SUBTASK,

                WRITE_TASK,

                READ_TASK,

                DISABLE_MANAGER_ROLE,

                ENABLE_MANAGER_ROLE,

                AFFECT_ROLE_PERMISSION,

                WRITE_SUPER_ADMIN,

                READ_SUPER_ADMIN,

                AFFECT_USERS_TO_PROJECT
        );
    }

    public List<ApplicationPermission> getRoleAndPermissionsOfManager() {
        return Arrays.asList(
                WRITE_PROJECT,

                READ_PROJECT,

                WRITE_SUBTASK,

                READ_SUBTASK,

                WRITE_TASK,

                READ_TASK,

                AFFECT_USERS_TO_PROJECT
        );
    }

    public List<ApplicationPermission> getRoleAndPermissionsOfAdmin() {
        return Arrays.asList(
                WRITE_USER,

                READ_USER,

                WRITE_MANAGER,

                READ_MANAGER
        );
    }


    public boolean checkValidationOfGivenEmail(String newEmail) {
        String EMAIL_VERIFICATION = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";
        return newEmail.matches(EMAIL_VERIFICATION);
    }

    public boolean validUserName(String newUsername) {
        return newUsername.matches("^[a-zA-Z]*$") && newUsername.length() > 7;
    }

    public boolean validUserPassword(String password) {
        return password.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]+$") && password.length() > 9;
    }

    public TaskManagerAdminHistoric logOperationOfAdmins(String username, String operation) {
        TaskManagerAdminHistoric taskManagerAdminHistoric = new TaskManagerAdminHistoric();
        taskManagerAdminHistoric.setOperation(operation);
        taskManagerAdminHistoric.setUsername(username);
        Instant nowUtc = Instant.now();
        ZoneId europaBerlin = ZoneId.of("Europe/Berlin");
        ZonedDateTime timestamp = ZonedDateTime.ofInstant(nowUtc, europaBerlin);
        taskManagerAdminHistoric.setTimestamp(timestamp);
        return taskManagerAdminHistoric;
    }

    public TaskManagerUserHistoric logOperationOfUsers(String username, String operation) {
        TaskManagerUserHistoric taskManagerUserHistoric = new TaskManagerUserHistoric();
        taskManagerUserHistoric.setOperation(operation);
        taskManagerUserHistoric.setUsername(username);
        Instant nowUtc = Instant.now();
        ZoneId europaBerlin = ZoneId.of("Europe/Berlin");
        ZonedDateTime timestamp = ZonedDateTime.ofInstant(nowUtc, europaBerlin);
        taskManagerUserHistoric.setTimestamp(timestamp);
        return taskManagerUserHistoric;
    }


    public void checkCountLocked(TaskManagerUserHistoric taskManagerUserLogger) {

        givenBadCredentialsMoreOrEqualThanThreeAndUnderThanTenMinutes(taskManagerUserLogger.getUsername(), taskManagerUserLogger.getOperation());

    }

    public void checkCountLockedAdmin(TaskManagerAdminHistoric taskManagerAdminHistoric) {

        givenBadCredentialsMoreOrEqualThanThreeAndUnderThanTenMinutesAdmin(taskManagerAdminHistoric.getUsername(), taskManagerAdminHistoric.getOperation());

    }

    public boolean givenBadCredentialsMoreOrEqualThanThreeAndUnderThanTenMinutes(String username, String operation) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        User user = null;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            if (!user.isAccountNonLocked()) {
                throw new UserLockoutException("This Account is locked for 24  Hours ");
            }
        }
        ZonedDateTime endTime = nowTime();
        ZonedDateTime startTime = endTime.minusMinutes(5);
        List<TaskManagerUserHistoric> loggers = taskManagerUserLoggerRepository.findByUsernameAndTimestampBetweenDate(username, startTime, endTime, operation);
        if (loggers != null) {
            List<TaskManagerUserHistoric> lastFiveOgUserHistoric = loggers.stream().limit(3).collect(Collectors.toList());
            if (lastFiveOgUserHistoric.size() >= 3 && lastFiveOgUserHistoric.stream().noneMatch(TaskManagerUserHistoric::isSuccessOperation)) {

                // save username in BlackList: Just one time after the 3 Times false login
                // Why? a user is succsfully log in. he has a valid token. but he tried to log in
                // 3 false log in => this user must not work with the token(valid)

                if (user != null) {
                    saveNewBlackListWithUsername(username,"BAD_CREDENTIALS");
                    user.setZonedDateTimeLockedUser(endTime);
                    user.setAccountNonLocked(false);
                    userRepository.save(user);
                    throw new UserLockoutException("You try to log in 3 times with an incorrect password. This user account is blocked at now for 24 hours.");
                }


            }
        }

        return false;
    }


    public boolean givenBadCredentialsMoreOrEqualThanThreeAndUnderThanTenMinutesAdmin(String username, String operation) {
        ZonedDateTime endTime = nowTime();
        ZonedDateTime startTime = endTime.minusMinutes(10);
        List<TaskManagerAdminHistoric> loggers = taskManagerAdminHistoricRepository.findByUsernameAndTimestampBetweenDate(username, startTime, endTime, operation);
        if (loggers != null) {
            List<TaskManagerAdminHistoric> lastFiveOgUserHistoric = loggers.stream().limit(3).collect(Collectors.toList());
            if (lastFiveOgUserHistoric.size() >= 3 && lastFiveOgUserHistoric.stream().noneMatch(TaskManagerAdminHistoric::isSuccessOperation)) {
                Optional<Admin> optionalAdmin = adminRepository.findAdminByUsername(username);
                if (optionalAdmin.isPresent()) {
                    Admin admin = optionalAdmin.get();
                    if (!admin.isAccountNonLocked()) {
                        throw new UserLockoutException("This Account is locked for 24 Hours ");
                    }
                    // save username in BlackList
                    saveNewBlackListWithUsername(username,"BAD_CREDENTIALS");
                    admin.setZonedDateTimeLockedUser(endTime);
                    admin.setAccountNonLocked(false);
                    adminRepository.save(admin);
                    throw new UserLockoutException("You try to log in five times with an incorrect password. This  Account is blocked at now for 24 hours.");
                }
            }
        }

        return false;
    }

    public ZonedDateTime nowTime() {
        Instant nowUtc = Instant.now();
        ZoneId europaBerlin = ZoneId.of("Europe/Berlin");
        return ZonedDateTime.ofInstant(nowUtc, europaBerlin);
    }

    public ZonedDateTime convertStringToZonedDateTime(String dateAsString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.GERMAN);
        LocalDate localDate = LocalDate.parse(dateAsString, formatter);
        ZoneId europaBerlin = ZoneId.of("Europe/Berlin");
        return localDate.atStartOfDay(europaBerlin);
    }

    public ZonedDateTime convertStringToZonedDateTime2(String dateAsString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN);
        LocalDateTime localDateTime = LocalDateTime.parse(dateAsString, formatter);
        ZoneId europaBerlin = ZoneId.of("Europe/Berlin");
        return localDateTime.atZone(europaBerlin);
    }

    public void saveNewBlackListWithUsername(String username, String cause) {
        BlacklistEntry blacklistEntry = new BlacklistEntry();
        // ZonedDateTime is time of now + the 24 Hours
        if (blacklistEntryRepository.findByUsername(username) == null) {
            ZonedDateTime expireTimeOfToken = nowTime().plusMinutes(10);
            blacklistEntry.setUsername(username);
            blacklistEntry.setExpiryTime(expireTimeOfToken);
            blacklistEntry.setCause(cause);
            blacklistEntryRepository.save(blacklistEntry);
        }

    }

    public boolean isValidDate(String date) {
        String regex = "^((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$";
        return Pattern.matches(regex, date);
    }

}
