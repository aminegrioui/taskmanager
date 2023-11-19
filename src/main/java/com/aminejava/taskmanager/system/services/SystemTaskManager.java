package com.aminejava.taskmanager.system.services;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.exception.user.AuthException;
import com.aminejava.taskmanager.exception.user.UserLockoutException;
import com.aminejava.taskmanager.repository.BlacklistEntryRepository;
import com.aminejava.taskmanager.system.entities.BlacklistEntry;
import com.aminejava.taskmanager.system.exception.BlacklistException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Slf4j
public class SystemTaskManager {
    private final BlacklistEntryRepository blacklistEntryRepository;
    private final AppTool appTool;

    public SystemTaskManager(BlacklistEntryRepository blacklistEntryRepository, AppTool appTool) {
        this.blacklistEntryRepository = blacklistEntryRepository;
        this.appTool = appTool;
    }

    // blackList logic

    @Scheduled(cron = "0 0/5 * * * ?", zone = "Europe/Berlin")
    public void scanBlackList() {
        log.info("SCAN_BLACK_LIST");
        List<BlacklistEntry> blacklistEntries = blacklistEntryRepository.findAll();
        for (BlacklistEntry blacklistEntry : blacklistEntries) {
            String token = blacklistEntry.getToken();
            ZonedDateTime nowTime = appTool.nowTime();
            BlacklistEntry removedBlackListEntry = blacklistEntryRepository.findByTokenAndExpiryTimeIsLessThanEqual(token, nowTime);

            if (removedBlackListEntry != null) {
                log.info("The removed token from BlackList is: " + token + " The expired time is: " + removedBlackListEntry.getExpiryTime() + "Now is: " + nowTime);
                blacklistEntryRepository.delete(blacklistEntry);
            }
            String username = blacklistEntry.getUsername();
            removedBlackListEntry = blacklistEntryRepository.findByUsernameAndExpiryTimeIsLessThanEqual(username, nowTime);
            if (removedBlackListEntry != null) {
                log.info("The removed username from BlackList is: " + username + " The expired time is: " + removedBlackListEntry.getExpiryTime() + "Now is: " + nowTime);
                blacklistEntryRepository.delete(blacklistEntry);
            }
        }
        log.info("END_SCAN_BLACK_LIST");
    }

    public void saveNewTokenToBlackList(String token) {
        log.info("SAVE_NEW_TOKEN_IN_BLACK_LIST");
        String bodyOfToken = token.substring(token.indexOf('.') + 1, 132);
        ZonedDateTime expireTimeOfToken = appTool.nowTime().plusMinutes(10);
        BlacklistEntry blacklistEntry = new BlacklistEntry();
        blacklistEntry.setToken(bodyOfToken);
        blacklistEntry.setExpiryTime(expireTimeOfToken);
        blacklistEntryRepository.save(blacklistEntry);
    }

    public void checkTokenInBlackList(String username, HttpHeaders requestHeader, HttpServletRequest request) {
        // this situation when the user has 3 times fialed log in: must wait the 24 Hours
        BlacklistEntry blacklistUserNameEntry = blacklistEntryRepository.findByUsername(username);
        if (blacklistUserNameEntry != null && blacklistUserNameEntry.getExpiryTime().isAfter(appTool.nowTime())) {
            throw new UserLockoutException("This Account is locked for 24 Hours ");
        }
        String token;
        if (request != null) {
            token = request.getHeader("Authorization").replace("Bearer", "");
        } else {
            List<String> values = requestHeader.get("Authorization");
            if (values == null || values.isEmpty()) {
                throw new AuthException("This request is unauthorized. It has no Token !!! ");
            }
            token = values.get(0).replace("Bearer", "");
        }


        if (Strings.isNullOrEmpty(token)) {
            throw new AuthException("This request is unauthorized. It has no Token !!! ");
        }


        String bodyOfToken = token.substring(token.indexOf('.') + 1, 132);
        BlacklistEntry blacklistTokenEntry = blacklistEntryRepository.findByTokenLike(bodyOfToken);
        if (blacklistTokenEntry != null && blacklistTokenEntry.getExpiryTime().isAfter(appTool.nowTime())) {
            throw new BlacklistException("The token is blacklisted and invalid. Please log in again or contact support");
        }

    }
}
