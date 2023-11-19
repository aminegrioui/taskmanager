package com.aminejava.taskmanager.exception.user;

import org.springframework.security.core.AuthenticationException;

public class UserLockoutException extends RuntimeException {
    public UserLockoutException(String message) {
        super(message);
    }
}
