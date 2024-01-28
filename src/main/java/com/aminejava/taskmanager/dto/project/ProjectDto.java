package com.aminejava.taskmanager.dto.project;


import com.aminejava.taskmanager.enums.Priority;
import lombok.Data;



@Data
public class ProjectDto {

    private String nameProject;

    private String department;

    private String projectStart;

    private String endProject;

    private String description;

    private Priority priority;
}
