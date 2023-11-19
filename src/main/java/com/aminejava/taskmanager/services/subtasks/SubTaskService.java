package com.aminejava.taskmanager.services.subtasks;

import com.aminejava.taskmanager.dto.subtask.DeleteSubTaskDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskRequestDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskUpdateDto;
import com.aminejava.taskmanager.enums.State;
import com.aminejava.taskmanager.dto.subtask.SubTaskResponseDto;
import com.aminejava.taskmanager.exception.ValidationDataException;
import com.aminejava.taskmanager.model.SubTask;
import com.aminejava.taskmanager.model.Task;
import com.aminejava.taskmanager.repository.SubTaskRepository;
import com.aminejava.taskmanager.repository.TaskRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
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
    private final JwtGenerator jwtGenerator;
    private final SystemTaskManager systemTaskManager;

    public SubTaskService(SubTaskRepository subTaskRepository, TaskRepository taskRepository, JwtGenerator jwtGenerator, SystemTaskManager systemTaskManager
    ) {
        this.subTaskRepository = subTaskRepository;
        this.taskRepository = taskRepository;
        this.jwtGenerator = jwtGenerator;
        this.systemTaskManager = systemTaskManager;
    }

    public SubTaskResponseDto saveSubTask(SubTaskRequestDto subTaskRequestDto, HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
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

        SubTask subTask = new SubTask();
        subTask.setTask(task);
        subTask.setSubTaskName(subTaskRequestDto.getSubTaskName());

        if (subTaskRequestDto.getState() == null) {
            subTask.setState(State.OPEN);
        } else {
            subTask.setState(subTaskRequestDto.getState());
        }
        subTaskRepository.save(subTask);
        SubTaskResponseDto subTaskDto= convertSubTaskToSubTaskDto(subTask, task.getName());
        subTaskDto.setTaskId(task.getTaskId());
        return subTaskDto;
    }

    public List<SubTask> getAllSubTasks(HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);
        return subTaskRepository.findAll().stream().filter(subTask -> !subTask.isDeleted()).collect(Collectors.toList());
    }

    public List<SubTask> getAllSubTaskOfTheAuthenticated(HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
        systemTaskManager.checkTokenInBlackList(parseTokenResponse.getUsername(), httpHeaders, null);

        if (parseTokenResponse.isManager()) {
            return subTaskRepository.findAll().stream().filter(subTask -> subTask.getTask().getProjectManager().getAdmin().getAdminId().longValue() ==
                    parseTokenResponse.getId() && !subTask.isDeleted()).collect(Collectors.toList());

//            Admin admin = adminRepository.findAdminByAdminId(parseTokenResponse.getId()).get();
//            Set<ProjectManager> projectManagers = admin.getProjectManagers();
//            List<SubTask> subTaskSet = new ArrayList<>();
//            for (ProjectManager projectManager : projectManagers) {
//                if (projectManager.getTasks() != null && !projectManager.getTasks().isEmpty()) {
//                    for (Task task : projectManager.getTasks()) {
//                        subTaskSet.addAll(task.getSubTasks());
//                    }
//                }
//            }
//          return subTaskSet;

        }

        return subTaskRepository.findAll().stream().filter(subTask -> subTask.getTask().getProject().getUser().getId().longValue() ==
                parseTokenResponse.getId() && !subTask.isDeleted()).collect(Collectors.toList());
    }

    public SubTaskResponseDto getSubTaskById(Long id, HttpHeaders httpHeaders) {
        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
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
            SubTaskResponseDto subTaskDto= convertSubTaskToSubTaskDto(subTask, task.getName());
            subTaskDto.setTaskId(task.getTaskId());
            return subTaskDto;
        }
        if (parseTokenResponse.getId().longValue() != task.getProject().getUser().getId()) {
            throw new ValidationDataException("This subtask belongs to another manager ");
        }
        SubTaskResponseDto subTaskDto= convertSubTaskToSubTaskDto(subTask, task.getName());
        subTaskDto.setTaskId(task.getTaskId());
        return subTaskDto;
    }

    @Transactional
    public SubTaskResponseDto updateSubTask(Long oldSubTaskId, SubTaskUpdateDto subTaskUpdateDto, HttpHeaders httpHeaders) {

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
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


        if (!Strings.isNullOrEmpty(subTaskUpdateDto.getSubTaskName())) {
            Set<SubTask> subTaskSet = task.getSubTasks();
            if (subTaskSet.contains(new SubTask(subTaskUpdateDto.getSubTaskName()))) {
                throw new ValidationDataException("A SubTask with this name: " + subTaskUpdateDto.getSubTaskName() + " is already saved in this Task: " + task.getTaskId());
            }
            subTask.setSubTaskName(subTaskUpdateDto.getSubTaskName());
        }

        if (subTaskUpdateDto.getState() != null) {
            subTask.setState(subTaskUpdateDto.getState());
        }

        SubTaskResponseDto subTaskDto= convertSubTaskToSubTaskDto(subTask, task.getName());
        subTaskDto.setTaskId(task.getTaskId());
        return subTaskDto;
    }

    @Transactional
    public DeleteSubTaskDto deleteSubTaskById(Long id, HttpHeaders httpHeaders) {

        ParseTokenResponse parseTokenResponse = jwtGenerator.getParseTokenResponse();
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
        }
        else{
            if (parseTokenResponse.getId().longValue() != task.getProject().getUser().getId()) {
                throw new ValidationDataException("You can't delete this subTask from this task: " + task.getName() + " this task is not yours ");
            }
        }

        if (subTask.isDeleted()) {
            throw new ValidationDataException("subTask with with id: " + id + " is already  deleted");
        }
        subTask.setDeleted(true);

        return new DeleteSubTaskDto(true, "The Subtask with id: " + id + " is deleted");
    }

    private SubTaskResponseDto convertSubTaskToSubTaskDto(SubTask subTask, String taskName) {
        SubTaskResponseDto subTaskDto = new SubTaskResponseDto();
        subTaskDto.setTaskName(taskName);
        subTaskDto.setSubTaskName(subTask.getSubTaskName());
        subTaskDto.setState(subTask.getState());


        return subTaskDto;
    }


}
