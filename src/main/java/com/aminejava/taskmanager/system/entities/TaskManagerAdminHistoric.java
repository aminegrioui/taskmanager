package com.aminejava.taskmanager.system.entities;


import lombok.Data;

import jakarta.persistence.*;
import java.time.ZonedDateTime;


@Entity
@Data
public class TaskManagerAdminHistoric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;
    private ZonedDateTime timestamp;
    private String username;
    private String operation;
    @Lob
    private String responseBody;
    private String errorMessage;
    private boolean isSuccessOperation;
}
