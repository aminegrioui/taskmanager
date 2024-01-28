package com.aminejava.taskmanager.model;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Data
public class UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userDetailsId;

    private String firstName;
    private String lastName;
    private String birthday;
    private String land;
    private String address;
    private String description;
    private String phone;
    private String mobile;
    private String job;

    @Column(name = "image_path")
    private String imagePath;
}
