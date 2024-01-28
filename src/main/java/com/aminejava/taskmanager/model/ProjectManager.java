package com.aminejava.taskmanager.model;

import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.model.admin.Admin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "projects_of_manager")
@Getter
@Setter
@NoArgsConstructor
public class ProjectManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;
    private String nameProject;
    private String department;
    private ZonedDateTime projectStart;
    private ZonedDateTime endProject;
    private Priority priority;
    @JsonIgnore
    private boolean deleted;
    @Lob
    private String description;

    @ManyToMany(mappedBy = "projectManagerList")
     private Set<User> users;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Admin admin;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "projectManager", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Task> tasks;

    public ProjectManager(String nameProject) {
        this.nameProject = nameProject;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        ProjectManager project = (ProjectManager) o;

        return (getNameProject() != null && Objects.equals(getNameProject(), project.getNameProject()));
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 32;
        result = PRIME * getNameProject().length() * result;
        return result;
    }


}
