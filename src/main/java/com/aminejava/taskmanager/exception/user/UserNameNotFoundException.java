package com.aminejava.taskmanager.exception.user;

import org.springframework.security.core.AuthenticationException;

public class UserNameNotFoundException extends AuthenticationException {
    public UserNameNotFoundException(String message) {
        super(message);
    }
}
