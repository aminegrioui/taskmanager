package com.aminejava.taskmanager.services.adminmamagment;

import com.aminejava.taskmanager.dto.management.manager.GroupOfWorksResponseDto;
import com.aminejava.taskmanager.dto.management.manager.ShortUserResponseDto;
import com.aminejava.taskmanager.dto.management.manager.UserManagerProjectAffectationDto;
import com.aminejava.taskmanager.dto.management.manager.UserProjectResponseDto;
import com.aminejava.taskmanager.dto.project.DeleteProjectResponseDto;
import com.aminejava.taskmanager.dto.project.ProjectResponseDto;
import com.aminejava.taskmanager.dto.project.ProjectDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskRequestDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskResponseDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskUpdateDto;
import com.aminejava.taskmanager.dto.task.DeleteTaskResponseDto;
import com.aminejava.taskmanager.dto.task.TaskDto;
import com.aminejava.taskmanager.dto.task.TaskResponseDto;
import com.aminejava.taskmanager.dto.task.TaskUpdateDto;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.ProjectManager;
import com.aminejava.taskmanager.model.SubTask;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.ProjectManagerRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtTool;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.aminejava.taskmanager.services.project.ProjectManagerService;
import com.aminejava.taskmanager.system.services.SystemTaskManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ManagerService {

    private final JwtTool jwtTool;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final ProjectManagerService projectManagerService;
    private final ProjectManagerRepository projectManagerRepository;
    private final SystemTaskManager systemTaskManager;

    public ManagerService(
            JwtTool jwtTool,
            UserRepository userRepository,
            AdminRepository adminRepository,
            ProjectManagerService projectManagerService,
            ProjectManagerRepository projectManagerRepository, SystemTaskManager systemTaskManager) {
        this.jwtTool = jwtTool;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.projectManagerService = projectManagerService;
        this.projectManagerRepository = projectManagerRepository;
        this.systemTaskManager = systemTaskManager;
    }

    public ProjectResponseDto createProjectOfManager(ProjectDto projectDto, HttpHeaders httpHeaders) {
        return projectManagerService.saveProjectOfManager(projectDto, httpHeaders);
    }

    public ProjectResponseDto updateProjectOfManager(Long projectManagerId, ProjectDto projectUpdateDto, HttpHeaders httpHeaders) {
        return projectManagerService.updateProjectOfManager(projectManagerId, projectUpdateDto, httpHeaders);
    }

    public List<ProjectResponseDto> getAllProjectOfManager(HttpHeaders httpHeaders) {
        return projectManagerService.getProjectsOfTheAuthenticatedManager(httpHeaders);
    }

    public ProjectResponseDto getProjectManagerById(Long projectId, HttpHeaders httpHeaders) {
        return projectManagerService.getProjectById(projectId, httpHeaders);
    }

    public DeleteProjectResponseDto deleteProjectManagerById(Long id, HttpHeaders httpHeaders) {
        return projectManagerService.deleteProjectManagerById(id, httpHeaders);
    }


    // #################### Task

    public TaskResponseDto createTaskOfProjectManager(TaskDto taskDto, HttpHeaders httpHeaders) {
        return projectManagerService.saveTaskOfProjectManager(taskDto, httpHeaders);
    }

    public TaskResponseDto updateTaskOfProjectManager(Long idTask, TaskUpdateDto taskDto, HttpHeaders httpHeaders) {
        return projectManagerService.updateTaskOfProjectManager(idTask, taskDto, httpHeaders);
    }

    public List<TaskResponseDto> getAllTaskOfThisManager(HttpHeaders httpHeaders) {
        return projectManagerService.getAllTasksOfManager(httpHeaders);
    }

    public TaskResponseDto getTaskOfManagerById(Long id, HttpHeaders httpHeaders) {
        return projectManagerService.getTaskById(id, httpHeaders);
    }

    public DeleteTaskResponseDto deleteTaskOfManagerById(Long id, HttpHeaders httpHeaders) {
        return projectManagerService.deleteTaskById(id, httpHeaders);
    }

    // #################### Task

    public SubTaskResponseDto saveSubTaskFromManager(SubTaskRequestDto subTaskRequestDto, HttpHeaders httpHeaders) {
        return projectManagerService.saveSubTaskFromManager(subTaskRequestDto, httpHeaders);
    }

    public List<SubTask> getAllSubTaskOfATask(HttpHeaders httpHeaders) {
        return projectManagerService.getAllSubTaskOfManager(httpHeaders);
    }

    public SubTaskResponseDto updateSubTask(Long oldSubTaskId, SubTaskUpdateDto subTaskUpdateDto, HttpHeaders httpHeaders) {
        return projectManagerService.updateSubTaskOfManager(oldSubTaskId, subTaskUpdateDto, httpHeaders);
    }

    public SubTaskResponseDto getSubTaskById(Long id, HttpHeaders httpHeaders) {
        return projectManagerService.getSubTaskById(id, httpHeaders);
    }

    public ResponseEntity<?> deleteSubTaskById(Long id, HttpHeaders httpHeaders) {
        return ResponseEntity.status(HttpStatus.OK).body(projectManagerService.deleteSubTaskById(id, httpHeaders));
    }

    // ############   Affect Users to the ProjectManager

    public UserProjectResponseDto affectUsersToTheProject(UserManagerProjectAffectationDto userProjectDto, HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        UserProjectResponseDto userProjectResponseDto = new UserProjectResponseDto();
        if (userProjectDto.getProjectId() == null || userProjectDto.getUserIdSet() == null || userProjectDto.getUserIdSet().isEmpty()) {
            throw new ValidationDataException("The request needs  project Id and set of users Id to handle this ");
        }

        Optional<ProjectManager> optionalProjectManager = projectManagerRepository.findProjectManagerByProjectId(userProjectDto.getProjectId());

        if (optionalProjectManager.isEmpty()) {
            throw new ValidationDataException("The given projectManager id not found ");
        }
        if (optionalProjectManager.get().isDeleted()) {
            throw new ValidationDataException("The given project id already deleted ");
        }

        ProjectManager projectManager = optionalProjectManager.get();

        if (projectManager.getAdmin().getAdminId().longValue() != parseTokenResponse.getId()) {
            throw new ValidationDataException("The manager id " + parseTokenResponse.getId() + " can not affect this project:  " + userProjectDto.getProjectId());
        }

        // Manager
        Optional<Admin> optionalManager = adminRepository.findAdminByAdminId(parseTokenResponse.getId());
        Admin manager = optionalManager.get();

        // Admin of Manager
        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(manager.getCreatedId());
        Admin adminOfManager = optionalAdmin.get();
        Set<User> usersOfAdmin = adminOfManager.getUsers();

        if (usersOfAdmin.isEmpty()) {
            throw new ValidationDataException("Your admin " + adminOfManager.getUsername() + " has no users. Contact ihm to create users ");
        }
        List<String> usernames = new ArrayList<>();
        for (Long idUser : userProjectDto.getUserIdSet()) {
            Optional<User> optionalUser = userRepository.findUserById(idUser);

            if (optionalUser.isEmpty() || optionalUser.get().isDeleted()) {
                throw new ValidationDataException("The user id " + idUser + " is not found or already deleted");
            }

            if (!usersOfAdmin.contains(new User(optionalUser.get().getUsername()))) {
                continue;
            }

            User user = optionalUser.get();
            Set<User> userSet = projectManager.getUsers();

            userSet.add(user);
            projectManager.setUsers(userSet);
            projectManagerRepository.save(projectManager);

            Set<ProjectManager> projectManagers = user.getProjectManagerList();
            projectManagers.add(projectManager);
            user.setProjectManagerList(projectManagers);
            userRepository.save(user);
            usernames.add(user.getUsername());
        }

        if (usernames.isEmpty()) {
            throw new ValidationDataException("Those given Users don't below to this manager");
        }

        userProjectResponseDto.setUsernamesOfUsers(usernames);
        userProjectResponseDto.setProjectNameOfManager(projectManager.getNameProject());
        userProjectResponseDto.setManagerName(optionalManager.get().getUsername());
        return userProjectResponseDto;
    }

    public Set<GroupOfWorksResponseDto> getGroupWorksOfManager(HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        Set<GroupOfWorksResponseDto> groupOfWorksResponseDtos = new HashSet<>();

        Optional<Admin> optionalManager = adminRepository.findAdminByAdminId(parseTokenResponse.getId());

        Admin manager = optionalManager.get();

        Set<ProjectManager> projectManagers = manager.getProjectManagers();
        if (projectManagers == null || projectManagers.isEmpty()) {
            throw new ValidationDataException("You have any projects");
        }
        for (ProjectManager projectManager : projectManagers) {
            Set<User> users = projectManager.getUsers();
            if (users == null || users.isEmpty()) {

                continue;
            }
            GroupOfWorksResponseDto groupOfWorksResponseDto = new GroupOfWorksResponseDto();
            groupOfWorksResponseDto.setProjectManagerName(projectManager.getNameProject());
            Set<ShortUserResponseDto> usersName = new HashSet<>();
            users.forEach(user -> usersName.add(new ShortUserResponseDto(user.getId(), user.getUsername())));
            groupOfWorksResponseDto.setUsers(usersName);
            groupOfWorksResponseDtos.add(groupOfWorksResponseDto);
        }
        if (groupOfWorksResponseDtos.isEmpty()) {
            throw new ValidationDataException("This Manager " + manager.getUsername() + " has no groupe of works");
        }

        return groupOfWorksResponseDtos;
    }

}
