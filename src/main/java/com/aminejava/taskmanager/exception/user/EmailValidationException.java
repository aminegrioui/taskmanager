package com.aminejava.taskmanager.exception.user;

public class EmailValidationException extends RuntimeException{
    public EmailValidationException(String message){
        super(message);
    }
}
