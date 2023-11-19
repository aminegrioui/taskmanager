package com.aminejava.taskmanager.dto.project;

import com.aminejava.taskmanager.enums.Priority;
import lombok.Data;

@Data
public class ProjectAddDto {

    private String nameProject;

    private String department;

    private String projectStart;

    private String projectEnd;

    private String description;

    private Priority priority;
}
