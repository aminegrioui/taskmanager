package com.aminejava.taskmanager.services.tasks;

import com.aminejava.taskmanager.dto.task.TaskResponseDto;
import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.enums.State;
import com.aminejava.taskmanager.dto.task.TaskDto;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.Project;
import com.aminejava.taskmanager.model.SubTask;
import com.aminejava.taskmanager.model.Task;
import com.aminejava.taskmanager.repository.ProjectRepository;
import com.aminejava.taskmanager.repository.SubTaskRepository;
import com.aminejava.taskmanager.repository.TaskRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.jwt.JwtTool;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.aminejava.taskmanager.system.services.SystemTaskManager;
import com.google.common.base.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final JwtTool jwtTool;
    private final SystemTaskManager systemTaskManager;
    private final SubTaskRepository subTaskRepository;


    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       JwtTool jwtTool, SystemTaskManager systemTaskManager,
                       SubTaskRepository subTaskRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.jwtTool = jwtTool;
        this.systemTaskManager = systemTaskManager;

        this.subTaskRepository = subTaskRepository;
    }

    public TaskResponseDto saveTask(TaskDto taskDto, HttpHeaders requestHeader) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        if (taskDto.getProjectId() == null) {
            throw new ValidationDataException("This Task must have a project");
        }

        Optional<Project> optionalProject = projectRepository.findById(taskDto.getProjectId());

        if (optionalProject.isEmpty()) {
            throw new ValidationDataException("A Project with this id: " + taskDto.getProjectId() + " is not found");
        }

        Project project = optionalProject.get();

        if (project.isDeleted()) {
            throw new ValidationDataException("A Project with this id: " + taskDto.getProjectId() + " is already deleted");
        }


        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            throw new ValidationDataException("Can't add Task to this project. This project belongs to another User");
        }

        Set<Task> tasks = project.getTasks();

        if (Strings.isNullOrEmpty(taskDto.getName())) {
            throw new ValidationDataException("Task must have a Name");
        }

        if (tasks.contains(new Task(taskDto.getName()))) {
            throw new ValidationDataException("A Task with this name: " + taskDto.getName() + " is already saved in this Project: " + taskDto.getProjectId());
        }

        Task task = new Task();
        task.setProject(project);

        task.setName(taskDto.getName());

        task.setState(State.OPEN);

        if (taskDto.getPriority() == null) {
            task.setPriority(Priority.LOW);
        } else {
            task.setPriority(taskDto.getPriority());
        }

        if (!Strings.isNullOrEmpty(taskDto.getDescription())) {
            task.setDescription(taskDto.getDescription());
        }
        // create direct the first subtask: every task must have at minimum one subtask

        SubTask firstSubtask = new SubTask();
        firstSubtask.setSubTaskName(task.getName());
        firstSubtask.setState(State.OPEN);
        Set<SubTask> subTaskSet = new HashSet<>();
        subTaskSet.add(firstSubtask);
        task.setSubTasks(subTaskSet);


        Task savedTask = taskRepository.save(task);
        firstSubtask.setTask(savedTask);
        subTaskRepository.save(firstSubtask);

        return convertTaskToTaskResponseDto(savedTask);
    }


    public List<TaskResponseDto> getAllTasks(HttpHeaders requestHeader) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        if (parseTokenResponse.isSuperAdmin()) {
            return taskRepository.findAll().stream().filter(task -> !task.isDeleted())
                    .map(this::convertTaskToTaskResponseDto)
                    .collect(Collectors.toList());
        }

        return taskRepository.findAll().stream().filter(task ->
                        task.getProject().getUser().getId().longValue() == parseTokenResponse.getId() && !task.isDeleted())
                .map(this::convertTaskToTaskResponseDto)
                .collect(Collectors.toList());

    }

    public List<TaskResponseDto> getAllTaskOfProject(Long projectId, HttpHeaders requestHeader) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isEmpty()) {
            throw new ValidationDataException("This project with project Id: " + projectId + " id not found ");
        }

        Project project = optionalProject.get();

        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            throw new ValidationDataException("Can't show tasks of this project");
        }

        if (project.isDeleted()) {
            throw new ValidationDataException("This project with project Id: " + projectId + " id deleted ");
        }


        return project.getTasks().stream().filter(task -> !task.isDeleted())
                .map(this::convertTaskToTaskResponseDto)
                .collect(Collectors.toList());

    }

    public TaskResponseDto getTaskById(Long id, HttpHeaders requestHeader) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            throw new ValidationDataException("Task with with id: " + id + " is not found");
        }

        Project project = optionalTask.get().getProject();

        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            throw new ValidationDataException("This Task belongs to another User ");
        }


        if (optionalTask.get().isDeleted()) {
            throw new ValidationDataException("Task with with id: " + id + " is already deleted");
        }
        return convertTaskToTaskResponseDto(optionalTask.get());
    }


    @Transactional
    public TaskResponseDto updateTask(Long taskId, TaskDto taskDto, HttpHeaders requestHeader) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        // Check if Token in BlackList
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), requestHeader, null);

        if (taskId == null || taskId <= 0) {
            throw new ValidationDataException("To update a project must be given a valid projectId");
        }
        Optional<Task> optionalTask = taskRepository.findById(taskId);

        if (optionalTask.isEmpty()) {
            throw new ValidationDataException("Task with with taskId: " + taskId + " is not found");
        }

        Task task = optionalTask.get();
        Project project = task.getProject();

        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            throw new ValidationDataException("This Task belongs to another User ");
        }

        if (optionalTask.get().isDeleted()) {
            throw new ValidationDataException("Task with with name: " + task.getName() + " is already deleted");
        }


        if (!Strings.isNullOrEmpty(taskDto.getName())) {
            task.setName(taskDto.getName());
        }

        // task has itself no state. it has just name. in other word the task is in OPN,IN_PROGRESS, Done
        // if this task has no subtask. it will in Response eine subtask with state Open

        if (taskDto.getPriority() != null) {
            task.setPriority(taskDto.getPriority());
        }
        if (!Strings.isNullOrEmpty(taskDto.getDescription())) {
            task.setDescription(taskDto.getDescription());
        }

        return convertTaskToTaskResponseDto(task);
    }

    @Transactional
    public Boolean deleteTaskById(Long id) {
        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            throw new ValidationDataException("Task with with id: " + id + " is not found");
        }

        Task task = optionalTask.get();
        Project project = task.getProject();
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();

        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            throw new ValidationDataException("This Task belongs to another User ");
        }

        if (optionalTask.get().isDeleted()) {
            throw new ValidationDataException("Task with with id: " + id + " is already  deleted");
        }


        optionalTask.get().getSubTasks().forEach(subTask -> subTask.setDeleted(true));
        optionalTask.get().setDeleted(true);
        return Boolean.TRUE;
    }

    private TaskResponseDto convertTaskToTaskResponseDto(Task task) {
        TaskResponseDto taskResponseDto = new TaskResponseDto();
        taskResponseDto.setTaskId(task.getTaskId());
        taskResponseDto.setProjectId(task.getProject().getProjectId());
        taskResponseDto.setName(task.getName());
        taskResponseDto.setState(task.getState());
        taskResponseDto.setPriority(task.getPriority());
        taskResponseDto.setDescription(task.getDescription());
        if (task.getSubTasks() == null || task.getSubTasks().stream().allMatch(SubTask::isDeleted)) {
            taskResponseDto.setSubTaskSet(createSetSubTask(task).stream().filter(s->!s.isDeleted()).collect(Collectors.toSet()));
        } else {
            taskResponseDto.setSubTaskSet(task.getSubTasks().stream().filter(s->!s.isDeleted()).collect(Collectors.toSet()));
        }

        return taskResponseDto;
    }

    private Set<SubTask> createSetSubTask(Task task) {
        SubTask firstSubtask = new SubTask();
        firstSubtask.setSubTaskName(task.getName());
        firstSubtask.setState(State.OPEN);
        firstSubtask.setTask(task);
        Set<SubTask> subTaskSet = task.getSubTasks();
        subTaskSet.add(firstSubtask);
        task.setSubTasks(subTaskSet);
        Task savedTask = taskRepository.save(task);
//        firstSubtask.setTask(savedTask);
//        subTaskRepository.save(firstSubtask);
        return subTaskSet;
    }

    public ResponseEntity<Task> getTaskOfProjectById(Long projectId, Long id) {
        Optional<Project> optionalProject = projectRepository.findProjectByProjectId(projectId);

        optionalProject.orElseThrow(() -> new NoSuchElementException("A project with this id: " + projectId + " is not found "));

        Optional<Task> optionalTask = taskRepository.findByProject(optionalProject.get());

        optionalTask.orElseThrow(() -> new NoSuchElementException("This project with this id: " + projectId + " has  no task with id " + id));

        return ResponseEntity.status(HttpStatus.OK).body(optionalTask.get());
    }
}
