package com.aminejava.taskmanager.exception.user;

public class AlreadyExistUserException extends RuntimeException {
    public AlreadyExistUserException(String message) {
        super(message);
    }
}
