package com.aminejava.taskmanager.services.tasks;

import com.aminejava.taskmanager.dto.task.DeleteTaskResponseDto;
import com.aminejava.taskmanager.dto.task.TaskResponseDto;
import com.aminejava.taskmanager.dto.task.TaskUpdateDto;
import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.enums.State;
import com.aminejava.taskmanager.dto.task.TaskAddDto;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.Project;
import com.aminejava.taskmanager.model.SubTask;
import com.aminejava.taskmanager.model.Task;
import com.aminejava.taskmanager.repository.ProjectRepository;
import com.aminejava.taskmanager.repository.TaskRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.jwt.ParseTokenResponse;
import com.google.common.base.Strings;
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
    private final JwtGenerator jwtGenerator;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, JwtGenerator jwtGenerator) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.jwtGenerator = jwtGenerator;
    }

    public TaskResponseDto saveTask(TaskAddDto taskDto) {

        TaskResponseDto taskDtoResponse = new TaskResponseDto();
        if (taskDto.getProjectId() == null) {
            throw new ValidationDataException("This Task must have a project");
        }
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

        Optional<Project> optionalProject = projectRepository.findById(taskDto.getProjectId());

        if (optionalProject.isEmpty()) {
            throw new ValidationDataException("A Project with this id: " + taskDto.getProjectId() + " is not found");
        }

        Project project = optionalProject.get();

        if (project.isDeleted()) {
            throw new ValidationDataException("A Project with this id: " + taskDto.getProjectId() + " is already deleted");
        }


        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            throw new ValidationDataException("Can't Task to this project. This project belongs to another User");
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


    public List<TaskResponseDto> getAllTasks() {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
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

    public List<TaskResponseDto> getAllTaskOfProject(Long projectId) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        TaskAddDto taskDtoResponse = new TaskAddDto();
        if (optionalProject.isEmpty()) {
            taskDtoResponse.setErrorMessage("This project with project Id: " + projectId + " id not found ");
            return null;
        }

        Project project = optionalProject.get();
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            taskDtoResponse.setErrorMessage("Can't show tasks of this project");
            return null;
        }

        if (project.isDeleted()) {
            taskDtoResponse.setErrorMessage("This project with project Id: " + projectId + " id deleted ");
            return null;
        }


        return taskRepository.findAll().stream().filter(task -> task.getProject().getProjectId().longValue() == projectId)
                .map(this::convertTaskToTaskResponseDto)
                .collect(Collectors.toList());

    }

    public TaskResponseDto getTaskById(Long id) {

        Optional<Task> optionalTask = taskRepository.findById(id);

        TaskResponseDto taskDtoResponse = new TaskResponseDto();
        if (optionalTask.isEmpty()) {
            throw new ValidationDataException("Task with with id: " + id + " is not found");
        }

        Project project = optionalTask.get().getProject();
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            throw new ValidationDataException("This Task belongs to another User ");
        }


        if (optionalTask.get().isDeleted()) {
            throw new ValidationDataException("Task with with id: " + id + " is already deleted");
        }
        return convertTaskToTaskResponseDto(optionalTask.get());
    }


    @Transactional
    public TaskResponseDto updateTask(TaskUpdateDto taskUpdateDto) {
        Optional<Task> optionalTask = taskRepository.findByName(taskUpdateDto.getName());

        TaskResponseDto taskDtoResponse = new TaskResponseDto();
        if (optionalTask.isEmpty()) {
            throw new ValidationDataException("Task with with name: " + taskUpdateDto.getName() + " is not found");
        }

        Task task = optionalTask.get();
        Project project = task.getProject();
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            throw new ValidationDataException("This Task belongs to another User ");
        }

        if (optionalTask.get().isDeleted()) {
            throw new ValidationDataException("Task with with name: " + taskUpdateDto.getName() + " is already deleted");
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

    @Transactional
    public DeleteTaskResponseDto deleteTaskById(Long id) {
        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            return new DeleteTaskResponseDto(false, "Task with with id: " + id + " is not found");
        }

        Task task = optionalTask.get();
        Project project = task.getProject();
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();

        if (parseTokenResponse.getId().longValue() != project.getUser().getId()) {
            return new DeleteTaskResponseDto(false, "This Task belongs to another User ");
        }

        if (optionalTask.get().isDeleted()) {
            return new DeleteTaskResponseDto(false, "Task with with id: " + id + " is already  deleted");
        }


        optionalTask.get().getSubTasks().forEach(subTask -> subTask.setDeleted(true));
        optionalTask.get().setDeleted(true);
        return new DeleteTaskResponseDto(true, "The Task with id: " + id + " is deleted ");
    }

    private TaskResponseDto convertTaskToTaskResponseDto(Task task) {
        TaskResponseDto taskResponseDto = new TaskResponseDto();
        taskResponseDto.setSubTaskSet(task.getSubTasks());
        taskResponseDto.setProjectId(task.getProjectManager().getProjectId());
        taskResponseDto.setName(task.getName());
        taskResponseDto.setPriority(task.getPriority());
        taskResponseDto.setState(task.getState());
        taskResponseDto.setDescription(task.getDescription());
        return taskResponseDto;
    }

    public ResponseEntity<Task> getTaskOfProjectById(Long projectId, Long id) {
        Optional<Project> optionalProject = projectRepository.findProjectByProjectId(projectId);

        optionalProject.orElseThrow(() -> new NoSuchElementException("A project with this id: " + projectId + " is not found "));

        Optional<Task> optionalTask = taskRepository.findByProject(optionalProject.get());

        optionalTask.orElseThrow(() -> new NoSuchElementException("This project with this id: " + projectId + " has  no task with id " + id));

        return ResponseEntity.status(HttpStatus.OK).body(optionalTask.get());
    }
}
