package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.model.Project;
import com.aminejava.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByNameProject(String nameProject);

    Optional<Project> findByUser(User user);

    Optional<Project> findProjectByProjectId(Long projectId);
}