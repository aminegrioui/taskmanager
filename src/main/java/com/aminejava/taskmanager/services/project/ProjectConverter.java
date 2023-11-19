package com.aminejava.taskmanager.services.project;


import com.aminejava.taskmanager.dto.project.ProjectResponseDto;
import com.aminejava.taskmanager.model.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectConverter {

    public ProjectResponseDto convertToProjectResponseDto(Project project) {

        return new ProjectResponseDto(project.getNameProject(),
                project.getDescription(), project.getUser().getUsername(), project.getPriority(),
                project.getProjectStart(), project.getEndProject(),
                project.getTasks());
    }
}
