package com.aminejava.taskmanager.dto.project;

import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.model.Task;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
public class ProjectResponseDto {
    private String projectName;
    private String description;
    private String userName;
    private Priority priority;
    private ZonedDateTime projectStart;
    private ZonedDateTime projectEnd;
    private Set<Task> tasks;


    public ProjectResponseDto(String projectName,
                              String description,
                              String userName,
                              Priority priority,
                              ZonedDateTime projectStart,
                              ZonedDateTime endProject,
                              Set<Task> tasks) {
        this.projectName = projectName;
        this.description = description;
        this.userName = userName;
        this.priority = priority;
        this.projectStart = projectStart;
        this.projectEnd = endProject;
        this.tasks = tasks;
    }

//    private String errorResponse;
}
