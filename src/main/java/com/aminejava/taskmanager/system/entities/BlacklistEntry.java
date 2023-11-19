package com.aminejava.taskmanager.system.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
public class BlacklistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    @Lob
//    @Nullable
    @Column(columnDefinition="TEXT")
    private String token;
    private String username;

    private ZonedDateTime expiryTime;

}
