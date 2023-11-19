package com.aminejava.taskmanager.services.project;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.dto.project.DeleteProjectResponseDto;
import com.aminejava.taskmanager.dto.project.ProjectAddDto;
import com.aminejava.taskmanager.dto.project.ProjectResponseDto;
import com.aminejava.taskmanager.dto.project.ProjectUpdateDto;
import com.aminejava.taskmanager.dto.subtask.DeleteSubTaskDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskRequestDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskResponseDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskUpdateDto;
import com.aminejava.taskmanager.dto.task.DeleteTaskResponseDto;
import com.aminejava.taskmanager.dto.task.TaskAddDto;
import com.aminejava.taskmanager.dto.task.TaskResponseDto;
import com.aminejava.taskmanager.dto.task.TaskUpdateDto;
import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.enums.State;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.ProjectManager;
import com.aminejava.taskmanager.model.SubTask;
import com.aminejava.taskmanager.model.Task;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.ProjectManagerRepository;
import com.aminejava.taskmanager.repository.SubTaskRepository;
import com.aminejava.taskmanager.repository.TaskRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.aminejava.taskmanager.services.subtasks.SubTaskService;
import com.aminejava.taskmanager.system.services.SystemTaskManager;
import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProjectManagerService {

    private final ProjectManagerRepository projectManagerRepository;
    private final JwtGenerator jwtGenerator;
    private final AdminRepository adminRepository;
    private final TaskRepository taskRepository;
    private final SubTaskService subTaskService;
    private final AppTool appTool;
    private final SystemTaskManager systemTaskManager;

    public ProjectManagerService(ProjectManagerRepository projectManagerRepository, JwtGenerator jwtGenerator, AdminRepository adminRepository, TaskRepository taskRepository, SubTaskRepository subTaskRepository, SubTaskService subTaskService, AppTool appTool, SystemTaskManager systemTaskManager) {
        this.projectManagerRepository = projectManagerRepository;
        this.jwtGenerator = jwtGenerator;
        this.adminRepository = adminRepository;
        this.taskRepository = taskRepository;
        this.subTaskService = subTaskService;
        this.appTool = appTool;
        this.systemTaskManager = systemTaskManager;
    }

    // Feature of ProjectManager

    @Transactional
    public ProjectResponseDto saveProjectOfManager(ProjectAddDto projectDto, HttpHeaders httpHeaders) {

        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        if (Strings.isNullOrEmpty(projectDto.getNameProject())) {
            throw new ValidationDataException("You must give a Name for the Project ");
        }

        if (Strings.isNullOrEmpty(projectDto.getDescription())) {
            throw new ValidationDataException("The Project must have a Description");
        }

        ProjectManager projectManager = new ProjectManager();

        projectManager.setNameProject(projectDto.getNameProject());
        projectManager.setDescription(projectDto.getDescription());

        if (!Strings.isNullOrEmpty(projectDto.getProjectEnd())) {
            if (!isValidDate(projectDto.getProjectEnd())) {
                throw new ValidationDataException("The given end date is not valid: The correct date is: yyyy-mm-dd");
            }
            projectManager.setEndProject(appTool.convertStringToZonedDateTime(projectDto.getProjectEnd()));
            if (projectManager.getEndProject().isBefore(appTool.nowTime())) {
                throw new ValidationDataException("The given end date must be in the future");
            }
        }
        if (!Strings.isNullOrEmpty(projectDto.getProjectStart())) {
            if (!isValidDate(projectDto.getProjectStart())) {
                throw new ValidationDataException("The start end date is not valid: The correct date is: yyyy-mm-dd");
            }
            projectManager.setProjectStart(appTool.convertStringToZonedDateTime(projectDto.getProjectStart()));
            if (projectManager.getEndProject().isBefore(projectManager.getProjectStart())) {
                throw new ValidationDataException("The end date must be after start date");
            }
        } else {
            projectManager.setProjectStart(appTool.nowTime());
        }
        if (projectDto.getPriority() != null) {
            projectManager.setPriority(projectDto.getPriority());
        }
        if (!Strings.isNullOrEmpty(projectDto.getDepartment())) {
            projectManager.setDepartment(projectDto.getDepartment());
        }
        Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());

        Set<ProjectManager> projectManagers = optionalAdmin.get().getProjectManagers();

        // without deleted projects
        if (projectManagers.contains(new ProjectManager(projectDto.getNameProject()))) {
            throw new ValidationDataException("This Project with this name is already created ");
        }

        projectManager.setAdmin(optionalAdmin.get());

        return convertProjectToProjectResponseDto(projectManagerRepository.save(projectManager));

    }

    @Transactional
    public ProjectResponseDto updateProjectOfManager(Long projectManagerId, ProjectUpdateDto newProject, HttpHeaders httpHeaders) {
        // Check if Token in BlackList
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<ProjectManager> optionalProject = projectManagerRepository.findById(projectManagerId);

        if (optionalProject.isEmpty() || optionalProject.get().isDeleted()) {
            throw new ValidationDataException("A Project with  this name: " + newProject.getNameProject() + " is not found or deleted");
        }

        ProjectManager projectManager = optionalProject.get();
        if (parseTokenResponse.getId().longValue() != projectManager.getAdmin().getAdminId()) {
            throw new ValidationDataException("You can't update this  ProjectManager. It is for another Manager");
        }

        if (newProject.getTasks() != null && !newProject.getTasks().isEmpty()) {
            Set<Task> tasks = projectManager.getTasks();
            newProject.getTasks().stream().filter(task -> task.getState() == null).forEach(task -> task.setState(State.OPEN));

            for (TaskAddDto taskDto : newProject.getTasks()) {
                if (!Strings.isNullOrEmpty(taskDto.getName())) {
                    Task task = convertTaskDtoToTask(taskDto);
                    task.setProjectManager(projectManager);
                    tasks.add(task);
                }
            }
            projectManager.setTasks(tasks);
        }

        if (!Strings.isNullOrEmpty(newProject.getNameProject())) {
            Optional<Admin> optionalAdmin = adminRepository.findAdminByAdminId(parseTokenResponse.getId());
            Set<ProjectManager> projectManagers = optionalAdmin.get().getProjectManagers();

            // without deleted projects
            if (projectManagers.contains(new ProjectManager(newProject.getNameProject()))) {
                throw new ValidationDataException("This Project with this name is already created ");
            }
            projectManager.setNameProject(newProject.getNameProject());
        }
        if (!Strings.isNullOrEmpty(newProject.getProjectEnd())) {
            if (!isValidDate(newProject.getProjectEnd())) {
                throw new ValidationDataException("The given end date is not valid: The correct date is: yyyy-mm-dd");
            }
            projectManager.setEndProject(appTool.convertStringToZonedDateTime(newProject.getProjectEnd()));
            if (projectManager.getEndProject().isBefore(appTool.nowTime())) {
                throw new ValidationDataException("The given end date must be in the future");
            }
        }
        if (!Strings.isNullOrEmpty(newProject.getProjectStart())) {
            if (!isValidDate(newProject.getProjectStart())) {
                throw new ValidationDataException("The start end date is not valid: The correct date is: yyyy-mm-dd");
            }
            projectManager.setProjectStart(appTool.convertStringToZonedDateTime(newProject.getProjectStart()));
            if (projectManager.getEndProject().isBefore(projectManager.getProjectStart())) {
                throw new ValidationDataException("The end date must be after start date");
            }
        }
        if (newProject.getPriority() != null) {
            projectManager.setPriority(newProject.getPriority());
        }
        if (!Strings.isNullOrEmpty(newProject.getDepartment())) {
            projectManager.setDepartment(newProject.getDepartment());
        }

        if (!Strings.isNullOrEmpty(newProject.getDescription())) {
            projectManager.setDescription(newProject.getDescription());
        }

        return convertProjectToProjectResponseDto(projectManager);
    }

    public List<ProjectResponseDto> getProjectsOfTheAuthenticatedManager(HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        return
                projectManagerRepository.findAll().
                        stream().filter(project -> project.getAdmin().getAdminId().longValue()
                                == parseTokenResponse.getId() && !project.isDeleted())
                        .map(this::convertProjectToProjectResponseDto)
                        .collect(Collectors.toList());

    }


    public ProjectResponseDto getProjectById(Long projectId, HttpHeaders httpHeaders) {

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<ProjectManager> optionalProjectManager = projectManagerRepository.findById(projectId);
        if (optionalProjectManager.isEmpty() || optionalProjectManager.get().isDeleted()) {
            throw new ValidationDataException("A Project with  this id: " + projectId + " is not found or already deleted");
        }
        if (parseTokenResponse.getId().longValue() != optionalProjectManager.get().getAdmin().getAdminId()) {
            throw new ValidationDataException("Project belongs to another Manager");
        }

        return convertProjectToProjectResponseDto(optionalProjectManager.get());

    }

    @Transactional
    public DeleteProjectResponseDto deleteProjectManagerById(Long id, HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<ProjectManager> optionalProjectManager = projectManagerRepository.findById(id);
        if (optionalProjectManager.isEmpty()) {
            throw new ValidationDataException("A Project with  this id: " + id + " is not found");
        }


        Long managerId = optionalProjectManager.get().getAdmin().getAdminId();
        if (parseTokenResponse.getId().longValue() != managerId) {
            throw new ValidationDataException("Can't delete this Project. It belongs to another Manager");
        }


        if (optionalProjectManager.get().isDeleted()) {
            throw new ValidationDataException("Project with with id: " + id + " is already  deleted");
        }
        optionalProjectManager.get().getTasks().forEach(task -> task.setDeleted(true));
        optionalProjectManager.get().setDeleted(true);
        return new DeleteProjectResponseDto(true, "The Project with id: " + id + " is deleted");
    }


    // feature Task:
    @Transactional
    public TaskResponseDto saveTaskOfProjectManager(TaskAddDto taskDto, HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        if (taskDto.getProjectManagerId() == null) {
            throw new ValidationDataException("This Task must have a projectManager id");
        }

        Optional<ProjectManager> projectManagerByProject = projectManagerRepository.findProjectManagerByProjectId(taskDto.getProjectManagerId());

        if (projectManagerByProject.isEmpty()) {
            throw new ValidationDataException("A Project with this id: " + taskDto.getProjectManagerId() + " is not found");
        }

        ProjectManager projectManager = projectManagerByProject.get();

        if (projectManager.isDeleted()) {
            throw new ValidationDataException("A ProjectManager with this id: " + taskDto.getProjectManagerId() + " is already deleted");
        }

        if (parseTokenResponse.getId().longValue() != projectManager.getAdmin().getAdminId()) {
            throw new ValidationDataException("You can't change/add to the given projectManager, it is for another manager");
        }


        if (Strings.isNullOrEmpty(taskDto.getName())) {
            throw new ValidationDataException("Task must have a Name");
        }

        Set<Task> tasks = projectManager.getTasks();
        if (tasks.contains(new Task(taskDto.getName()))) {
            throw new ValidationDataException("A Task with this name: " + taskDto.getName() + " is already saved in this ProjectManager: " + projectManager.getNameProject() + " in this Manager: " + parseTokenResponse.getUsername());
        }

        Task task = new Task();
        task.setProjectManager(projectManager);

        task.setName(taskDto.getName());

        if (taskDto.getState() == null) {
            task.setState(State.OPEN);
        } else {
            task.setState(taskDto.getState());
        }
        if (taskDto.getPriority() == null) {
            task.setPriority(Priority.LOW);
        } else {
            task.setPriority(taskDto.getPriority());
        }

        if (!Strings.isNullOrEmpty(taskDto.getDescription())) {
            task.setDescription(taskDto.getDescription());
        }

        return convertTaskToTaskResponseDto(taskRepository.save(task));
    }

    @Transactional
    public TaskResponseDto updateTaskOfProjectManager(Long taskId, TaskUpdateDto taskUpdateDto, HttpHeaders httpHeaders) {

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<Task> optionalTask = taskRepository.findById(taskId);

        if (optionalTask.isEmpty()) {
            throw new ValidationDataException("Task with with this name: " + taskUpdateDto.getName() + " is not found");
        }

        Task task = optionalTask.get();
        ProjectManager projectManager = task.getProjectManager();

        if (parseTokenResponse.getId().longValue() != projectManager.getAdmin().getAdminId()) {
            throw new ValidationDataException("You can't update this task. This Task belongs to an otter ProjectManager  ");
        }

        if (task.isDeleted()) {
            throw new ValidationDataException("Task with with name " + taskUpdateDto.getName() + " is already deleted");
        }


        if (taskUpdateDto.getSubTaskSet() != null && !taskUpdateDto.getSubTaskSet().isEmpty()) {
            Set<SubTask> subTaskSet = task.getSubTasks();
            taskUpdateDto.getSubTaskSet().stream().filter(subTask -> subTask.getState() == null).forEach(subTask -> subTask.setState(State.OPEN));
            for (SubTask subTask : taskUpdateDto.getSubTaskSet()) {
                if (!Strings.isNullOrEmpty(subTask.getSubTaskName())) {
                    subTask.setTask(task);
                    subTaskSet.add(subTask);
                }
            }

            task.setSubTasks(subTaskSet);

        }
        if (!Strings.isNullOrEmpty(taskUpdateDto.getName())) {
            Set<Task> tasks = projectManager.getTasks();
            if (tasks.contains(new Task(taskUpdateDto.getName()))) {
                throw new ValidationDataException("A Task with this name: " + taskUpdateDto.getName() + " is already saved in this ProjectManager: " + projectManager.getNameProject() + " in this Manager: " + parseTokenResponse.getUsername());
            }
            task.setName(taskUpdateDto.getName());
        }

        if (taskUpdateDto.getPriority() != null) {
            task.setPriority(taskUpdateDto.getPriority());
        }

        if (taskUpdateDto.getState() != null) {
            task.setState(taskUpdateDto.getState());
        }
        if (!Strings.isNullOrEmpty(taskUpdateDto.getDescription())) {
            task.setDescription(taskUpdateDto.getDescription());
        }


        return convertTaskToTaskResponseDto(task);
    }

    public List<TaskResponseDto> getAllTasksOfManager(HttpHeaders httpHeaders) {

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        return taskRepository.findAll().stream().filter(task ->
                        task.getProjectManager().getAdmin().getAdminId().longValue() == parseTokenResponse.getId()
                                &&
                                !task.isDeleted())
                .map(this::convertTaskToTaskResponseDto)
                .collect(Collectors.toList());

    }

    public TaskResponseDto getTaskById(Long id, HttpHeaders httpHeaders) {

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            throw new ValidationDataException("Task with with id: " + id + " is not found");
        }

        ProjectManager projectManager = optionalTask.get().getProjectManager();

        if (parseTokenResponse.getId().longValue() != projectManager.getAdmin().getAdminId()) {
            throw new ValidationDataException("This Task belongs to another Manager ");
        }


        if (optionalTask.get().isDeleted()) {
            throw new ValidationDataException("Task with with id: " + id + " is already deleted");
        }
        return convertTaskToTaskResponseDto(optionalTask.get());
    }

    @Transactional
    public DeleteTaskResponseDto deleteTaskById(Long id, HttpHeaders httpHeaders) {

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            throw new ValidationDataException("Task with with id: " + id + " is not found");
        }

        Task task = optionalTask.get();
        ProjectManager projectManager = task.getProjectManager();


        if (parseTokenResponse.getId().longValue() != projectManager.getAdmin().getAdminId()) {
            throw new ValidationDataException("This Task belongs to another Manager ");
        }

        if (optionalTask.get().isDeleted()) {
            throw new ValidationDataException("Task with with id: " + id + " is already  deleted");
        }


        optionalTask.get().getSubTasks().forEach(subTask -> subTask.setDeleted(true));
        optionalTask.get().setDeleted(true);
        return new DeleteTaskResponseDto(true, "The Task with id: " + id + " is deleted ");
    }

    // Feature of SubTasks
    public SubTaskResponseDto saveSubTaskFromManager(SubTaskRequestDto subTaskRequestDto, HttpHeaders httpHeaders) {
        return subTaskService.saveSubTask(subTaskRequestDto,httpHeaders);
    }

    public List<SubTask> getAllSubTaskOfManager(HttpHeaders httpHeaders) {
        return subTaskService.getAllSubTaskOfTheAuthenticated(httpHeaders);
    }

    public SubTaskResponseDto updateSubTaskOfManager(Long oldSubTaskId, SubTaskUpdateDto subTaskUpdateDto, HttpHeaders httpHeaders) {
        return subTaskService.updateSubTask(oldSubTaskId, subTaskUpdateDto,httpHeaders);
    }

    public SubTaskResponseDto getSubTaskById(Long id, HttpHeaders httpHeaders) {
        return subTaskService.getSubTaskById(id,httpHeaders);
    }

    public DeleteSubTaskDto deleteSubTaskById(Long id,HttpHeaders httpHeaders) {
        return subTaskService.deleteSubTaskById(id,httpHeaders);
    }

    private TaskResponseDto convertTaskToTaskResponseDto(Task task) {
        TaskResponseDto taskResponseDto = new TaskResponseDto();
        taskResponseDto.setSubTaskSet(task.getSubTasks());
        taskResponseDto.setProjectManagerId(task.getProjectManager().getProjectId());
        taskResponseDto.setName(task.getName());
        taskResponseDto.setPriority(task.getPriority());
        taskResponseDto.setState(task.getState());
        taskResponseDto.setDescription(task.getDescription());
        return taskResponseDto;
    }

    private Task convertTaskDtoToTask(TaskAddDto taskDto) {
        Task task = new Task();
        task.setName(taskDto.getName());
        task.setDescription(task.getDescription());
        task.setState(taskDto.getState());
        task.setPriority(task.getPriority());
        return task;
    }

    private ProjectResponseDto convertProjectToProjectResponseDto(ProjectManager projectManager) {
        ProjectResponseDto projectResponseDto = new ProjectResponseDto();
        projectResponseDto.setProjectName(projectManager.getNameProject());
        projectResponseDto.setPriority(projectManager.getPriority());
        projectResponseDto.setDescription(projectManager.getDescription());
        projectResponseDto.setUserName(projectManager.getAdmin().getUsername());
        projectResponseDto.setProjectEnd(projectManager.getEndProject());
        projectResponseDto.setProjectStart(projectManager.getProjectStart());
        projectResponseDto.setTasks(projectManager.getTasks());
        return projectResponseDto;
    }

    private boolean isValidDate(String date) {
        String regex = "^((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$";
        return Pattern.matches(regex, date);
    }
}
