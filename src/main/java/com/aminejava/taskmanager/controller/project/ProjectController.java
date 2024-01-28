package com.aminejava.taskmanager.controller.project;

import com.aminejava.taskmanager.dto.project.ProjectDto;
import com.aminejava.taskmanager.services.project.ProjectService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/taskmanager/v1/projects")
public class ProjectController {


    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/projectsOfUser")
    public ResponseEntity<?> getAllProjectOgUser(@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjectsOfTheAuthenticatedUser(requestHeader));
    }


    @PostMapping("/addProject")
    public ResponseEntity<?> saveProject(@RequestBody ProjectDto projectDto,@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.saveProject(projectDto,requestHeader));
    }

    @PutMapping("/updateProject/{projectId}")
    private ResponseEntity<?> updateProject(@PathVariable Long projectId, @RequestBody ProjectDto projectDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.updateProject(projectId,projectDto,requestHeader));
    }

    @GetMapping("/getProjectById/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id,@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjectById(id,requestHeader));
    }

    @DeleteMapping("/deleteProject/{id}")
    public ResponseEntity<?> deleteProjectById(@PathVariable Long id,@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.deleteProjectById(id,requestHeader));
    }
    @PutMapping("/setProjectToFinish/{id}")
    public ResponseEntity<?>  setProjectToFinish(@PathVariable Long id,@RequestHeader HttpHeaders requestHeader){
        return ResponseEntity.status(HttpStatus.OK).body(projectService.setProjectToFinish(id,requestHeader));
    }

}

