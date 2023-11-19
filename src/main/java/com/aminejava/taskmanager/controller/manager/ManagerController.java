package com.aminejava.taskmanager.controller.manager;

import com.aminejava.taskmanager.dto.management.manager.UserManagerProjectAffectationDto;
import com.aminejava.taskmanager.dto.project.ProjectAddDto;
import com.aminejava.taskmanager.dto.project.ProjectUpdateDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskRequestDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskUpdateDto;
import com.aminejava.taskmanager.dto.task.TaskAddDto;
import com.aminejava.taskmanager.dto.task.TaskUpdateDto;
import com.aminejava.taskmanager.services.adminmamagment.ManagerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/taskmanager/v1/manager/management")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @PostMapping("/createProject")
    public ResponseEntity<?> createProject(@RequestBody ProjectAddDto projectDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerService.createProjectOfManager(projectDto, requestHeader));
    }

    @PutMapping("/updateProjectManager/{projectId}")
    public ResponseEntity<?> updateProjectManager(@PathVariable Long projectId, @RequestBody ProjectUpdateDto projectUpdateDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerService.updateProjectOfManager(projectId, projectUpdateDto, requestHeader));
    }

    @GetMapping("/allProjectOfManager")
    public ResponseEntity<?> getAllProjectOfManager(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(managerService.getAllProjectOfManager(requestHeader));
    }

    @GetMapping("/projectManager/{projectId}")
    public ResponseEntity<?> getProjectManagerById(@PathVariable Long projectId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(managerService.getProjectManagerById(projectId, requestHeader));
    }

    @DeleteMapping("/delete/projectManager/{projectId}")
    public ResponseEntity<?> deleteProjectManagerById(@PathVariable Long projectId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(managerService.deleteProjectManagerById(projectId, requestHeader));
    }

    // RIGHT_READ Task

    @PostMapping("/createTask")
    public ResponseEntity<?> createTask(@RequestBody TaskAddDto taskAddDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerService.createTaskOfProjectManager(taskAddDto, requestHeader));
    }

    @PutMapping("/updateTaskOfManager/{taskId}")
    public ResponseEntity<?> updateTaskOfProjectManager(@PathVariable Long taskId, @RequestBody TaskUpdateDto taskUpdateDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerService.updateTaskOfProjectManager(taskId, taskUpdateDto, requestHeader));
    }

    @GetMapping("/allTasksOfManager")
    public ResponseEntity<?> getAllTaskOfThisManager(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(managerService.getAllTaskOfThisManager(requestHeader));
    }

    @GetMapping("/taskManager/{taskId}")
    public ResponseEntity<?> getTaskOfManagerById(@PathVariable Long taskId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(managerService.getTaskOfManagerById(taskId, requestHeader));
    }

    @DeleteMapping("/delete/taskManager/{taskId}")
    public ResponseEntity<?> deleteTaskOfManagerById(@PathVariable Long taskId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(managerService.deleteTaskOfManagerById(taskId, requestHeader));
    }

    // RIGHT_READ SubTask

    @PostMapping("/createSubTask")
    public ResponseEntity<?> createSubTask(@RequestBody SubTaskRequestDto subTaskRequestDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerService.saveSubTaskFromManager(subTaskRequestDto, requestHeader));
    }

    @PutMapping("/updateSubTaskOfManager/{subTaskId}")
    public ResponseEntity<?> updateSubTaskOfProjectManager(@PathVariable Long subTaskId, @RequestBody SubTaskUpdateDto subTaskUpdateDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerService.updateSubTask(subTaskId, subTaskUpdateDto, requestHeader));
    }

    @GetMapping("/allSubTasksOfManager")
    public ResponseEntity<?> getAllSubTaskOfThisManager(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(managerService.getAllSubTaskOfATask(requestHeader));
    }

    @GetMapping("/subTaskManager/{subTaskId}")
    public ResponseEntity<?> getSubTaskOfManagerById(@PathVariable Long subTaskId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(managerService.getSubTaskById(subTaskId, requestHeader));
    }

    @DeleteMapping("/delete/subTaskManager/{subTaskId}")
    public ResponseEntity<?> deleteSubTaskOfManagerById(@PathVariable Long subTaskId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(managerService.deleteSubTaskById(subTaskId, requestHeader));
    }

    //   Affect Users to the ProjectManager

    @PostMapping("/projectManager/affetctUsersToProject")
    public ResponseEntity<?> affectUsersToTheProject(@RequestBody UserManagerProjectAffectationDto userProjectDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerService.affectUsersToTheProject(userProjectDto, requestHeader));
    }

    @GetMapping("/projectManager/groupWorksOfManager")
    public ResponseEntity<?> getGroupWorksOfManager(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerService.getGroupWorksOfManager(requestHeader));
    }

}
