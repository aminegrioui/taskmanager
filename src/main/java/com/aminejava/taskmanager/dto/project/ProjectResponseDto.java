package com.aminejava.taskmanager.dto.project;

import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.model.Task;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class ProjectResponseDto {
    private String nameProject;
    private String description;
    private String userName;
    private Priority priority;
    private String projectStart;
    private String endProject;
    private String department;
    private Set<Task> tasks;


    public ProjectResponseDto(String projectName,
                              String description,
                              String userName,
                              Priority priority,
                              String projectStart,
                              String endProject,
                              Set<Task> tasks) {
        this.nameProject = projectName;
        this.description = description;
        this.userName = userName;
        this.priority = priority;
        this.projectStart = projectStart;
        this.endProject = endProject;
        this.tasks = tasks;
    }

//    private String errorResponse;
}
