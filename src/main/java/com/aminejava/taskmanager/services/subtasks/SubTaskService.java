package com.aminejava.taskmanager.services.subtasks;

import com.aminejava.taskmanager.dto.subtask.SubTaskRequestDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskUpdateDto;
import com.aminejava.taskmanager.enums.State;
import com.aminejava.taskmanager.dto.subtask.SubTaskResponseDto;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.SubTask;
import com.aminejava.taskmanager.model.Task;
import com.aminejava.taskmanager.repository.SubTaskRepository;
import com.aminejava.taskmanager.repository.TaskRepository;
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
public class SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;
    private final JwtTool jwtTool;
    private final SystemTaskManager systemTaskManager;

    public SubTaskService(SubTaskRepository subTaskRepository, TaskRepository taskRepository, JwtTool jwtTool, SystemTaskManager systemTaskManager
    ) {
        this.subTaskRepository = subTaskRepository;
        this.taskRepository = taskRepository;
        this.jwtTool = jwtTool;
        this.systemTaskManager = systemTaskManager;
    }

    @Transactional
    public SubTaskResponseDto saveSubTask(SubTaskRequestDto subTaskRequestDto, HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        if (subTaskRequestDto.getTaskId() == null) {
            throw new ValidationDataException("This SubTask must have a task");
        }
        if (Strings.isNullOrEmpty(subTaskRequestDto.getSubTaskName())) {
            throw new ValidationDataException("To create a subtask you have to give a name");
        }


        Optional<Task> optionalTask = taskRepository.findById(subTaskRequestDto.getTaskId());

        if (optionalTask.isEmpty() || optionalTask.get().isDeleted()) {
            throw new ValidationDataException("A SubTask with this id: " + subTaskRequestDto.getTaskId() + " is not found or deleted");
        }

        Task task = optionalTask.get();
        if (subTaskRequestDto.getSubTaskName().equals(task.getName())) {
            throw new ValidationDataException("This name: " + task.getName() + " is name of Task, please chose another name ");
        }
        if (parseTokenResponse.isManager()) {
            if (parseTokenResponse.getId().longValue() != task.getProjectManager().getAdmin().getAdminId()) {
                throw new ValidationDataException("You can't add this subTask to this task: " + task.getName() + " this task is not yours ");
            }
        } else {
            if (parseTokenResponse.getId().longValue() != task.getProject().getUser().getId()) {
                throw new ValidationDataException("You can't add this subTask to this task: " + task.getName() + " this task is not yours ");
            }
        }

        Set<SubTask> subTaskSet = optionalTask.get().getSubTasks();

        if (subTaskSet.contains(new SubTask(subTaskRequestDto.getSubTaskName()))) {
            throw new ValidationDataException("A SubTask with this name: " + subTaskRequestDto.getSubTaskName() + " is already saved in this Task: " + optionalTask.get().getTaskId());
        }
        // first time adding explizit a subtask: remove the first subtask with the name of Task
        Optional<SubTask> optionalSubTask = task.getSubTasks().stream().filter(subTask ->
                !subTask.isDeleted() && subTask.getSubTaskName().equals(task.getName())).findFirst();

        if (optionalSubTask.isPresent()) {
            task.getSubTasks().remove(optionalSubTask.get());
            taskRepository.save(task);
            subTaskRepository.deleteById(optionalSubTask.get().getIdSubTask());
        }

        SubTask subTask = new SubTask();
        subTask.setTask(task);
        subTask.setSubTaskName(subTaskRequestDto.getSubTaskName());
        subTask.setState(State.OPEN);
        subTask.setDescription(subTaskRequestDto.getDescription());
        subTaskRepository.save(subTask);
        SubTaskResponseDto subTaskDto = convertSubTaskToSubTaskDto(subTask, task.getName());
        subTaskDto.setTaskId(task.getTaskId());
        return subTaskDto;
    }

    public List<SubTask> getAllSubTasks(HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        return subTaskRepository.findAll().stream().filter(subTask -> !subTask.isDeleted()).collect(Collectors.toList());
    }

    public List<SubTaskResponseDto> getSubTasksOfTask(Long taskId, HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        if (taskId == null) {
            throw new ValidationDataException("You have to give a taskId");
        }
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isEmpty() || optionalTask.get().isDeleted()) {
            throw new ValidationDataException("This task: " + taskId + " is not found or already deleted ");
        }
        Task task = optionalTask.get();
        if (parseTokenResponse.getId().longValue() != task.getProject().getUser().getId()) {
            throw new ValidationDataException("You can't add this subTask to this task: " + task.getName() + " this task is not yours ");
        }
        // send task with set subtasks
        if (task.getSubTasks() == null || task.getSubTasks().stream().filter(subTask -> !subTask.isDeleted()).collect(Collectors.toList()).isEmpty()) {
            SubTask subTask = new SubTask();
            subTask.setTask(task);
            subTask.setState(State.OPEN);
            subTask.setSubTaskName(task.getName());
            subTaskRepository.save(subTask);
        }
        return task.getSubTasks().stream().filter(subTask -> !subTask.isDeleted())
                .map(subTask -> convertSubTaskToSubTaskDto(subTask, task.getName()))
                .collect(Collectors.toList());

    }

    public List<SubTask> getAllSubTaskOfTheAuthenticated(HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        if (parseTokenResponse.isManager()) {
            return subTaskRepository.findAll().stream().filter(subTask -> subTask.getTask().getProjectManager().getAdmin().getAdminId().longValue() ==
                    parseTokenResponse.getId() && !subTask.isDeleted()).collect(Collectors.toList());
        }

        return subTaskRepository.findAll().stream().filter(subTask -> subTask.getTask().getProject().getUser().getId().longValue() ==
                parseTokenResponse.getId() && !subTask.isDeleted()).collect(Collectors.toList());
    }

    public SubTaskResponseDto getSubTaskById(Long id, HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<SubTask> optionalSubTask = subTaskRepository.findByIdSubTask(id);

        if (optionalSubTask.isEmpty() || optionalSubTask.get().isDeleted()) {
            throw new ValidationDataException("This SubTask with this id: " + id + " is not found or deleted");
        }
        SubTask subTask = optionalSubTask.get();
        Task task = subTask.getTask();

        if (parseTokenResponse.isManager()) {
            if (parseTokenResponse.getId().longValue() != task.getProjectManager().getAdmin().getAdminId()) {
                throw new ValidationDataException("This subtask belongs to another manager ");
            }
            SubTaskResponseDto subTaskDto = convertSubTaskToSubTaskDto(subTask, task.getName());
            subTaskDto.setTaskId(task.getTaskId());
            return subTaskDto;
        }
        if (parseTokenResponse.getId().longValue() != task.getProject().getUser().getId()) {
            throw new ValidationDataException("This subtask belongs to another manager ");
        }
        // send task with set subtasks
        if (task.getSubTasks() == null || task.getSubTasks().stream().filter(s -> !s.isDeleted()).collect(Collectors.toList()).isEmpty()) {
            SubTask newSubTask = new SubTask();
            newSubTask.setTask(task);
            newSubTask.setState(State.OPEN);
            newSubTask.setSubTaskName(task.getName());
            subTaskRepository.save(newSubTask);
        }
        SubTaskResponseDto subTaskDto = convertSubTaskToSubTaskDto(subTask, task.getName());
        subTaskDto.setTaskId(task.getTaskId());
        return subTaskDto;
    }

    @Transactional
    public SubTaskResponseDto updateSubTask(Long oldSubTaskId, SubTaskUpdateDto subTaskUpdateDto, HttpHeaders httpHeaders) {

        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<SubTask> optionalSubTask = subTaskRepository.findByIdSubTask(oldSubTaskId);

        if (optionalSubTask.isEmpty() || optionalSubTask.get().isDeleted()) {
            throw new ValidationDataException("This SubTask with this id: " + oldSubTaskId + " is not found or deleted");
        }

        SubTask subTask = optionalSubTask.get();
        Task task = subTask.getTask();

        if (parseTokenResponse.isManager()) {
            if (parseTokenResponse.getId().longValue() != task.getProjectManager().getAdmin().getAdminId()) {
                throw new ValidationDataException("You can't add this subTask to this task: " + task.getName() + " this task is not yours ");
            }
        } else {
            if (parseTokenResponse.getId().longValue() != task.getProject().getUser().getId()) {
                throw new ValidationDataException("You can't add this subTask to this task: " + task.getName() + " this task is not yours ");
            }
        }

        if (!Strings.isNullOrEmpty(subTaskUpdateDto.getSubTaskName()) && subTaskUpdateDto.getSubTaskName().equals(task.getName())) {
            throw new ValidationDataException("This name: " + task.getName() + " is name of Task, please chose another name ");
        }
        if (!Strings.isNullOrEmpty(subTaskUpdateDto.getSubTaskName())) {
            subTask.setSubTaskName(subTaskUpdateDto.getSubTaskName().strip());
        }
        if (subTaskUpdateDto.getState() != null) {
            subTask.setState(subTaskUpdateDto.getState());
        }
        if(Strings.isNullOrEmpty(subTaskUpdateDto.getDescription())){
            subTask.setDescription(subTaskUpdateDto.getDescription());
        }

        SubTaskResponseDto subTaskDto = convertSubTaskToSubTaskDto(subTask, task.getName());
        subTaskDto.setTaskId(task.getTaskId());
        return subTaskDto;
    }

    @Transactional
    public Boolean deleteSubTaskById(Long id, HttpHeaders httpHeaders) {

        ParseTokenResponse parseTokenResponse = jwtTool.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        Optional<SubTask> optionalSubTask = subTaskRepository.findByIdSubTask(id);

        if (optionalSubTask.isEmpty()) {
            throw new ValidationDataException("subTask with with id: " + id + " is not found");
        }
        SubTask subTask = optionalSubTask.get();
        Task task = subTask.getTask();

        if (parseTokenResponse.isManager()) {
            if (parseTokenResponse.getId().longValue() != task.getProjectManager().getAdmin().getAdminId()) {
                throw new ValidationDataException("You can't delete this subTask from this task: " + task.getName() + " this task is not yours ");
            }
        } else {
            if (parseTokenResponse.getId().longValue() != task.getProject().getUser().getId()) {
                throw new ValidationDataException("You can't delete this subTask from this task: " + task.getName() + " this task is not yours ");
            }
        }

        if (subTask.isDeleted()) {
            throw new ValidationDataException("subTask with with id: " + id + " is already  deleted");
        }
        subTask.setDeleted(true);

        return Boolean.TRUE;
    }

    private SubTaskResponseDto convertSubTaskToSubTaskDto(SubTask subTask, String taskName) {
        SubTaskResponseDto subTaskDto = new SubTaskResponseDto();
        subTaskDto.setTaskName(taskName);
        subTaskDto.setSubTaskName(subTask.getSubTaskName());
        subTaskDto.setState(subTask.getState());
        subTaskDto.setTaskId(subTask.getTask().getTaskId());
        subTaskDto.setIdSubTask(subTask.getIdSubTask());
        subTaskDto.setDescription(subTask.getDescription());
        return subTaskDto;
    }


}
