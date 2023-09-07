package com.aminejava.taskmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class Permission {
    @Id
    @Column(name = "permession_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String permission;

}
