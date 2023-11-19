package com.aminejava.taskmanager.controller.project;

import com.aminejava.taskmanager.dto.project.ProjectAddDto;
import com.aminejava.taskmanager.dto.project.ProjectUpdateDto;
import com.aminejava.taskmanager.services.project.ProjectService;
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
    public ResponseEntity<?> getAllProjectOgUser() {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjectsOfTheAuthenticatedUser());
    }


    @PostMapping("/addProject")
    public ResponseEntity<?> saveProject(@RequestBody ProjectAddDto projectDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.saveProject(projectDto));
    }

    @PutMapping("/updateProject")
    private ResponseEntity<?> updateProject( @RequestBody ProjectUpdateDto projectDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.updateProject(projectDto));
    }

    @GetMapping("/getProjectById/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.getProjectById(id));
    }

    @DeleteMapping("/deleteProject/{id}")
    public ResponseEntity<?> deleteProjectById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(projectService.deleteProjectById(id));
    }

//    @GetMapping("/getProjectByProjectName/{projectName}")
//    public ResponseEntity<?> getProjectByProjectName(@PathVariable String projectName, @RequestHeader HttpHeaders headers) {
//        return projectService.findProjectByProjectName(projectName, headers);
//    }
}

