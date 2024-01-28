package com.aminejava.taskmanager.model;

import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Data
public class ProjectHistoric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private ZonedDateTime createdDate;
    private ZonedDateTime updatedDate;
    private ZonedDateTime finishedDate;
    private ZonedDateTime deletedDate;
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    private String oldValue;
    private String newValue;
}
