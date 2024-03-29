package com.aminejava.taskmanager.system.entities;

import lombok.Data;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
public class BlacklistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition="TEXT")
    private String token;
    private String username;
    private String cause;

    private ZonedDateTime expiryTime;

}
