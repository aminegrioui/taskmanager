package com.aminejava.taskmanager.services.project;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.project.ProjectDto;
import com.aminejava.taskmanager.model.Project;
import com.aminejava.taskmanager.model.ProjectHistoric;
import com.aminejava.taskmanager.repository.ProjectHistoricRepository;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ProjectHistoricService {
    private final ProjectHistoricRepository projectHistoricRepository;
    private final AppTool appTool;

    public ProjectHistoricService(ProjectHistoricRepository projectHistoricRepository, AppTool appTool) {
        this.projectHistoricRepository = projectHistoricRepository;
        this.appTool = appTool;
    }

    public void saveHistoricOfNewProject(Project project) {
        ProjectHistoric projectHistoric = new ProjectHistoric();
        projectHistoric.setCreatedDate(appTool.nowTime());
        projectHistoric.setProject(project);
        projectHistoricRepository.save(projectHistoric);
    }

    public ProjectHistoric saveUpdatedValuesOfProject(Project oldProjectValue, ProjectDto projectDto) {
        String oldValues = "";
        String updatedValues = "";
        ProjectHistoric projectHistoric = new ProjectHistoric();
        if (!Strings.isNullOrEmpty(projectDto.getNameProject())) {
            oldValues += " NameProject: " + oldProjectValue.getNameProject();
            updatedValues += " NameProject: " + projectDto.getNameProject();
        }
        if (!Strings.isNullOrEmpty(projectDto.getDescription())) {
            oldValues += " ,Description: " + oldProjectValue.getDescription();
            updatedValues += " ,Description: " + projectDto.getDescription();
        }
        if (!Strings.isNullOrEmpty(projectDto.getDepartment())) {
            oldValues += " ,Department: " + oldProjectValue.getDepartment();
            updatedValues += " ,Department: " + projectDto.getDepartment();
        }
        if (projectDto.getPriority()!=null && !Strings.isNullOrEmpty(projectDto.getPriority().name())) {
            oldValues += " ,Priority: " + oldProjectValue.getPriority().name();
            updatedValues += " ,Priority: " + projectDto.getPriority().name();
        }
        if (!Strings.isNullOrEmpty(projectDto.getEndProject())) {
            oldValues += " ,EndProject: " + oldProjectValue.getEndProject();
            updatedValues += " ,EndProject: " + projectDto.getEndProject();
        }
        if (!Strings.isNullOrEmpty(projectDto.getProjectStart())) {
            oldValues += " ,ProjectStart: " + oldProjectValue.getProjectStart();
            updatedValues += " ,ProjectStart: " + projectDto.getProjectStart();
        }
        projectHistoric.setOldValue(oldValues);
        projectHistoric.setNewValue(updatedValues);
        projectHistoric.setUpdatedDate(appTool.nowTime());
      return projectHistoric;
    }

    public ProjectHistoric saveDeletedDatumOfProject(Project project) {
        ProjectHistoric projectHistoric = new ProjectHistoric();
        projectHistoric.setDeletedDate(appTool.nowTime());
        projectHistoric.setProject(project);
        return projectHistoric;
    }
    public ProjectHistoric saveFinishedDatum(Project project){
        ProjectHistoric projectHistoric = new ProjectHistoric();
        projectHistoric.setFinishedDate(appTool.nowTime());
        projectHistoric.setProject(project);
        return projectHistoric;
    }

}
