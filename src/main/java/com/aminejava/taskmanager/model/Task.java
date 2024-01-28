package com.aminejava.taskmanager.model;

import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.enums.State;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;
    private String name;
    private Priority priority;
    private State state;
    @JsonIgnore
    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;


    @ManyToOne
    @JoinColumn(name = "project_manager_id")
    @JsonIgnore
    private ProjectManager projectManager;

    @Lob
    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "task", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<SubTask> subTasks = new HashSet<>();

    public Task(String name) {
        this.name = name;
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

        Task task = (Task) o;

        return (getName() != null && Objects.equals(getName(), task.getName()));
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 32;
        result = PRIME * getName().length() * result;
        return result;
    }

}
