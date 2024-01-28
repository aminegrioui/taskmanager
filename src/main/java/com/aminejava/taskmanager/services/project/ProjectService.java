package com.aminejava.taskmanager.services.project;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.project.*;
import com.aminejava.taskmanager.enums.State;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.Project;
import com.aminejava.taskmanager.model.ProjectHistoric;
import com.aminejava.taskmanager.model.Task;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.repository.ProjectHistoricRepository;
import com.aminejava.taskmanager.repository.ProjectRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.jwt.JwtTool;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.aminejava.taskmanager.system.services.SystemTaskManager;
import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {


    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final JwtTool jwtTool;
    private final AppTool appTool;
    private final ProjectConverter projectConverter;
    private final SystemTaskManager systemTaskManager;
    private final ProjectHistoricService projectHistoricService;
    private final ProjectHistoricRepository projectHistoricRepository;


    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, JwtTool jwtTool, AppTool appTool, ProjectConverter projectConverter, SystemTaskManager systemTaskManager, ProjectHistoricService projectHistoricService,
                          ProjectHistoricRepository projectHistoricRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.jwtTool = jwtTool;
        this.appTool = appTool;
        this.projectConverter = projectConverter;
        this.systemTaskManager = systemTaskManager;
        this.projectHistoricService = projectHistoricService;
        this.projectHistoricRepository = projectHistoricRepository;
    }

    public List<ProjectResponseDto> getAllProjects(HttpHeaders requestHeader) {

        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        return projectRepository.findAll().stream().filter(project -> !project.isDeleted())
                .map(projectConverter::convertToProjectResponseDto).collect(Collectors.toList());
    }


    public List<ShortProjectResponseDto> getProjectsOfTheAuthenticatedUser(HttpHeaders requestHeader) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);
        if (parseTokenResponse.isSuperAdmin()) {
            return
                    projectRepository.findAll().
                            stream().filter(project -> !project.isDeleted())
                            .map(project ->
                                    new ShortProjectResponseDto(project.getProjectId(),
                                            project.getNameProject(), project.getDescription(), project.getPriority()))
                            .sorted(Comparator.comparing(o -> o.getPriority().getPriorityNumber()))
                            .collect(Collectors.toList());
        }
        return
                projectRepository.findAll().
                        stream().filter(project -> project.getUser().getId().longValue() == parseTokenResponse.getId() && !project.isDeleted())
                        .map(project ->
                                new ShortProjectResponseDto(project.getProjectId(),
                                        project.getNameProject(), project.getDescription(), project.getPriority()))
                        .sorted(Comparator.comparing(o -> o.getPriority().getPriorityNumber()))
                        .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponseDto saveProject(ProjectDto projectDto, HttpHeaders requestHeader) {


        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);
        Long id = parseTokenResponse.getId();
        if (Strings.isNullOrEmpty(projectDto.getNameProject())) {
            throw new ValidationDataException("You must give a Name for the Project ");
        }

        if (Strings.isNullOrEmpty(projectDto.getDescription())) {
            throw new ValidationDataException("The Project must have a Description");
        }

        Project project = new Project();

        project.setNameProject(projectDto.getNameProject());
        project.setDescription(projectDto.getDescription());

        // EndProject : is planning to end project
        if (!Strings.isNullOrEmpty(projectDto.getEndProject())) {
            if (!appTool.isValidDate(projectDto.getEndProject())) {
                throw new ValidationDataException("The given end date is not valid: The correct date is: yyyy-mm-dd");
            }
            project.setEndProject(appTool.convertStringToZonedDateTime(projectDto.getEndProject()));
            if (project.getEndProject().isBefore(appTool.nowTime())) {
                throw new ValidationDataException("The given end date must be in the future");
            }
        }
        if (!Strings.isNullOrEmpty(projectDto.getProjectStart())) {
            if (!appTool.isValidDate(projectDto.getProjectStart())) {
                throw new ValidationDataException("The start end date is not valid: The correct date is: yyyy-mm-dd");
            }
            project.setProjectStart(appTool.convertStringToZonedDateTime(projectDto.getProjectStart()));
            if (project.getEndProject() != null && project.getEndProject().isBefore(project.getProjectStart())) {
                throw new ValidationDataException("The end date must be after start date");
            }
        } else {
            project.setProjectStart(appTool.nowTime());
        }
        if (projectDto.getPriority() != null) {
            project.setPriority(projectDto.getPriority());
        }
        if (!Strings.isNullOrEmpty(projectDto.getDepartment())) {
            project.setDepartment(projectDto.getDepartment());
        }

        Optional<User> optionalUser = userRepository.findUserById(id);

        Set<Project> projects = optionalUser.get().getProjects();

        if (projects.contains(new Project(projectDto.getNameProject()))) {
            throw new ValidationDataException("This Project with this name is already created ");
        }

        project.setUser(optionalUser.get());

        // new Project
        Project newProject = projectRepository.save(project);

        // Historic Project
        projectHistoricService.saveHistoricOfNewProject(newProject);
        return convertProjectToProjectResponseDto(newProject);
    }


    public ProjectResponseDto getProjectById(Long projectId, HttpHeaders requestHeader) {

        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<Project> optionalProject = projectRepository.findProjectByProjectId(projectId);
        if (optionalProject.isEmpty() || optionalProject.get().isDeleted()) {
            throw new ValidationDataException("A Project with  this id: " + projectId + " is not found or already deleted");
        }
        if (parseTokenResponse.getId().longValue() != optionalProject.get().getUser().getId()) {
            throw new ValidationDataException("Project belongs to another User");
        }

        return convertProjectToProjectResponseDto(optionalProject.get());

    }

    @Transactional
    public ProjectResponseDto updateProject(Long projectId, ProjectDto newProject, HttpHeaders requestHeader) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        if (projectId == null || projectId <= 0) {
            throw new ValidationDataException("To update a project must be given a valid projectId");
        }
        Optional<Project> optionalProject = projectRepository.findById(projectId);

        if (optionalProject.isEmpty() || optionalProject.get().isDeleted()) {
            throw new ValidationDataException("A Project with  this project: " + projectId + " is not found or deleted");
        }

        Project oldProject = optionalProject.get();
        Project trackOldProjectValue = new Project();
        Long userId = oldProject.getUser().getId();
        if (parseTokenResponse.getId().longValue() != userId) {
            throw new ValidationDataException("You can't access this request, The requested Project is for another User");
        }

        Optional<User> optionalUser = userRepository.findUserById(userId);

        if(!Strings.isNullOrEmpty(newProject.getNameProject())){
            trackOldProjectValue.setNameProject(oldProject.getNameProject());
            oldProject.setNameProject(newProject.getNameProject());
        }

        if (!Strings.isNullOrEmpty(newProject.getEndProject())) {
            if (!appTool.isValidDate(newProject.getEndProject())) {
                throw new ValidationDataException("The given end date is not valid: The correct date is: yyyy-mm-dd");
            }
            trackOldProjectValue.setEndProject(oldProject.getEndProject());
            oldProject.setEndProject(appTool.convertStringToZonedDateTime(newProject.getEndProject()));
            // track old values
            if (oldProject.getEndProject().isBefore(appTool.nowTime())) {
                throw new ValidationDataException("The given end date must be in the future");
            }
        }
        if (!Strings.isNullOrEmpty(newProject.getProjectStart())) {
            if (!appTool.isValidDate(newProject.getProjectStart())) {
                throw new ValidationDataException("The start end date is not valid: The correct date is: yyyy-mm-dd");
            }
            trackOldProjectValue.setProjectStart(oldProject.getProjectStart());
            oldProject.setProjectStart(appTool.convertStringToZonedDateTime(newProject.getProjectStart()));
            if (oldProject.getEndProject() != null && oldProject.getEndProject().isBefore(oldProject.getProjectStart())) {
                throw new ValidationDataException("The end date must be after start date");
            }
        }
        if (newProject.getPriority() != null) {
            trackOldProjectValue.setPriority(oldProject.getPriority());
            oldProject.setPriority(newProject.getPriority());
        }
        if (!Strings.isNullOrEmpty(newProject.getDepartment())) {
            trackOldProjectValue.setDepartment(oldProject.getDepartment());
            oldProject.setDepartment(newProject.getDepartment());
        }
        if (!Strings.isNullOrEmpty(newProject.getDescription())) {
            trackOldProjectValue.setDescription(oldProject.getDescription());
            oldProject.setDescription(newProject.getDescription());
        }

        // save historic of project
       ProjectHistoric projectHistoric=  projectHistoricService.saveUpdatedValuesOfProject(trackOldProjectValue, newProject);
        Set<ProjectHistoric> projectHistorics=oldProject.getProjectHistorics();
        projectHistorics.add(projectHistoric);
        oldProject.setProjectHistorics(projectHistorics);
        projectHistoric.setProject(oldProject);
        return convertProjectToProjectResponseDto(oldProject);
    }


    @Transactional
    public DeleteProjectResponseDto deleteProjectById(Long id, HttpHeaders requestHeader) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);
        Optional<Project> optionalProject = projectRepository.findProjectByProjectId(id);
        if (optionalProject.isEmpty()) {
            return new DeleteProjectResponseDto(false, "A Project with  this id: " + id + " is not found");
        }

        if (parseTokenResponse.getId().longValue() != optionalProject.get().getUser().getId()) {
            return new DeleteProjectResponseDto(false, "Can't delete this Project. It belongs to another User");
        }


        if (optionalProject.get().isDeleted()) {
            return new DeleteProjectResponseDto(false, "Project with with id: " + id + " is already  deleted");
        }
        optionalProject.get().getTasks().forEach(task -> task.setDeleted(true));
        optionalProject.get().setDeleted(true);
        // save historic of project
       ProjectHistoric projectHistoric= projectHistoricService.saveDeletedDatumOfProject(optionalProject.get());
       Set<ProjectHistoric> projectHistorics=optionalProject.get().getProjectHistorics();
       projectHistorics.add(projectHistoric);
       optionalProject.get().setProjectHistorics(projectHistorics);
       projectHistoric.setProject(optionalProject.get());
        return new DeleteProjectResponseDto(true, "The Project with id: " + id + " is deleted");
    }


    public ProjectResponseDto setProjectToFinish(Long projectId, HttpHeaders requestHeader) {
        if (projectId != null || projectId.longValue() < 0) {
            throw new ValidationDataException("give a projectId to make the project finished ");
        }
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);
        Optional<Project> optionalProject = projectRepository.findProjectByProjectId(projectId);
        if (optionalProject.isEmpty()) {
            throw new ValidationDataException("A Project with  this id: " + projectId + " is not found");
        }

        if (parseTokenResponse.getId().longValue() != optionalProject.get().getUser().getId()) {
            throw new ValidationDataException("Can't delete this Project. It belongs to another User");
        }

        if (optionalProject.get().isDeleted()) {
            throw new ValidationDataException("Project with with id: " + projectId + " is already  deleted");
        }
        Project project = optionalProject.get();
        Set<Task> tasks = project.getTasks();
        tasks.stream().forEach(task ->
        {
            task.setState(State.COMPLETED);
            if (task.getSubTasks() != null && !task.getSubTasks().isEmpty()) {
                task.getSubTasks().forEach(subTask -> subTask.setState(State.COMPLETED));
            }
        });
       ProjectHistoric projectHistoric=  projectHistoricService.saveFinishedDatum(project);
        Set<ProjectHistoric> projectHistorics=project.getProjectHistorics();
        projectHistorics.add(projectHistoric);
        project.setProjectHistorics(projectHistorics);
        projectHistoric.setProject(project);
        return convertProjectToProjectResponseDto(project);
    }
    private ProjectResponseDto convertProjectToProjectResponseDto(Project project) {
        ProjectResponseDto projectResponseDto = new ProjectResponseDto();
        projectResponseDto.setUserName(project.getUser().getUsername());
        projectResponseDto.setNameProject(project.getNameProject());
        projectResponseDto.setPriority(project.getPriority());
        projectResponseDto.setDescription(project.getDescription());
        projectResponseDto.setEndProject(project.getEndProject() + "");
        projectResponseDto.setProjectStart(project.getProjectStart() + "");
        projectResponseDto.setTasks(project.getTasks());
        projectResponseDto.setDepartment(project.getDepartment());
        return projectResponseDto;
    }


}
