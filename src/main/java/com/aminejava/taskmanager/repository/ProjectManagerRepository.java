package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.model.ProjectManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectManagerRepository extends JpaRepository<ProjectManager, Long> {

    Optional<ProjectManager> findProjectManagerByProjectId(Long projectId);

    Optional<ProjectManager> findProjectManagerByNameProject(String nameProject);
}