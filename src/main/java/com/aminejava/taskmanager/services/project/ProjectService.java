package com.aminejava.taskmanager.services.project;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.project.DeleteProjectResponseDto;
import com.aminejava.taskmanager.dto.project.ProjectResponseDto;
import com.aminejava.taskmanager.dto.project.ProjectUpdateDto;
import com.aminejava.taskmanager.dto.task.TaskAddDto;
import com.aminejava.taskmanager.enums.State;
import com.aminejava.taskmanager.dto.project.ProjectAddDto;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.Project;
import com.aminejava.taskmanager.model.Task;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.repository.ProjectRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.google.common.base.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {


    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    private final AppTool appTool;
    private final ProjectConverter projectConverter;


    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, JwtGenerator jwtGenerator, AppTool appTool, ProjectConverter projectConverter) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.appTool = appTool;
        this.projectConverter = projectConverter;
    }

    public List<ProjectResponseDto> getAllProjects() {
        return projectRepository.findAll().stream().filter(project -> !project.isDeleted())
                .map(projectConverter::convertToProjectResponseDto).collect(Collectors.toList());
    }


    public ResponseEntity<?> getProjectsOfTheAuthenticatedUser() {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        if (parseTokenResponse.isSuperAdmin()) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    projectRepository.findAll().
                            stream().filter(project -> !project.isDeleted())
                            .map(this::convertProjectToProjectResponseDto)
                            .collect(Collectors.toList()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(
                projectRepository.findAll().
                        stream().filter(project -> project.getUser().getId().longValue() == parseTokenResponse.getId() && !project.isDeleted())
                        .map(this::convertProjectToProjectResponseDto)
                        .collect(Collectors.toList()));
    }

    @Transactional
    public ProjectResponseDto saveProject(ProjectAddDto projectDto) {


        ProjectResponseDto projectResponseDto = new ProjectResponseDto();

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();


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

        if (!Strings.isNullOrEmpty(projectDto.getProjectEnd())) {
            // clean code: Can't end in the past, compare with start.

            project.setEndProject(appTool.convertStringToZonedDateTime(projectDto.getProjectEnd()));
        }
        if (!Strings.isNullOrEmpty(projectDto.getProjectStart())) {
            // clean code: Can't start in the past
            project.setProjectStart(appTool.convertStringToZonedDateTime(projectDto.getProjectStart()));
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

        return convertProjectToProjectResponseDto(projectRepository.save(project));
    }


    public ProjectResponseDto getProjectById(Long projectId) {

        ProjectResponseDto projectResponseDto = new ProjectResponseDto();
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

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
    public ProjectResponseDto updateProject(ProjectUpdateDto newProject) {

        ProjectResponseDto projectResponseDto = new ProjectResponseDto();
        Optional<Project> optionalProject = projectRepository.findByNameProject(newProject.getNameProject());

        if (optionalProject.isEmpty() || optionalProject.get().isDeleted()) {
            throw new ValidationDataException("A Project with  this name: " + newProject.getNameProject() + " is not found or deleted");
        }

        Project oldProject = optionalProject.get();
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        Long idUserOrAdmin = oldProject.getUser().getId();
        if (parseTokenResponse.getId().longValue() != idUserOrAdmin) {
            throw new ValidationDataException("You can't access this request, The requested Project is for another User");
        }


        if (newProject.getTasks() != null && !newProject.getTasks().isEmpty()) {
            Set<Task> tasks = oldProject.getTasks();
            newProject.getTasks().stream().filter(task -> task.getState() == null).forEach(task -> task.setState(State.OPEN));

            for (TaskAddDto taskDto : newProject.getTasks()) {
                if (!Strings.isNullOrEmpty(taskDto.getName())) {
                    Task task = convertTaskDtoToTask(taskDto);
                    task.setProject(oldProject);
                    tasks.add(task);
                }
            }
            oldProject.setTasks(tasks);
        }

        if (!Strings.isNullOrEmpty(newProject.getNameProject())) {
            oldProject.setNameProject(newProject.getNameProject());
        }
        if (!Strings.isNullOrEmpty(newProject.getProjectEnd())) {
            oldProject.setEndProject(appTool.convertStringToZonedDateTime(newProject.getProjectEnd()));
        }
        if (!Strings.isNullOrEmpty(newProject.getProjectStart())) {
            oldProject.setProjectStart(appTool.convertStringToZonedDateTime(newProject.getProjectStart()));
        }
        if (newProject.getPriority() != null) {
            oldProject.setPriority(newProject.getPriority());
        }
        if (!Strings.isNullOrEmpty(newProject.getDepartment())) {
            oldProject.setDepartment(newProject.getDepartment());
        }

        if (!Strings.isNullOrEmpty(newProject.getDescription())) {
            oldProject.setDescription(newProject.getDescription());
        }

        return convertProjectToProjectResponseDto(oldProject);
    }


    @Transactional
    public DeleteProjectResponseDto deleteProjectById(Long id) {
        Optional<Project> optionalProject = projectRepository.findProjectByProjectId(id);
        if (optionalProject.isEmpty()) {
            return new DeleteProjectResponseDto(false, "A Project with  this id: " + id + " is not found");
        }

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        if (parseTokenResponse.getId().longValue() != optionalProject.get().getUser().getId()) {
            return new DeleteProjectResponseDto(false, "Can't delete this Project. It belongs to another User");
        }


        if (optionalProject.get().isDeleted()) {
            return new DeleteProjectResponseDto(false, "Project with with id: " + id + " is already  deleted");
        }
        optionalProject.get().getTasks().forEach(task -> task.setDeleted(true));
        optionalProject.get().setDeleted(true);
        return new DeleteProjectResponseDto(true, "The Project with id: " + id + " is deleted");
    }

    private ProjectResponseDto convertProjectToProjectResponseDto(Project project) {
        ProjectResponseDto projectResponseDto = new ProjectResponseDto();
        projectResponseDto.setUserName(project.getUser().getUsername());
        projectResponseDto.setProjectName(project.getNameProject());
        projectResponseDto.setPriority(project.getPriority());
        projectResponseDto.setDescription(project.getDescription());
        return projectResponseDto;
    }

    private Task convertTaskDtoToTask(TaskAddDto taskDto) {
        Task task = new Task();
        task.setName(taskDto.getName());
        task.setDescription(task.getDescription());
        task.setState(taskDto.getState());
        task.setPriority(task.getPriority());
        return task;
    }


}
