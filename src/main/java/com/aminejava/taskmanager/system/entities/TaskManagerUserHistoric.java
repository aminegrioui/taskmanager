package com.aminejava.taskmanager.system.entities;

import lombok.Data;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
public class TaskManagerUserHistoric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private ZonedDateTime timestamp;
    private String username;
    private String operation;
    private String responseBody;
    private String errorMessage;
    private boolean isSuccessOperation;
    private String role;
}
