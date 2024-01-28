package com.aminejava.taskmanager.model;

import com.aminejava.taskmanager.enums.State;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "subtasks")
@Data
@NoArgsConstructor
public class SubTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long idSubTask;

    private String subTaskName;

    private State state;

    private String description;
    @JsonIgnore
    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    public SubTask(String name) {
        this.subTaskName = name;
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


        SubTask subTask = (SubTask) o;
        return (getSubTaskName() != null && Objects.equals(getSubTaskName(), subTask.getSubTaskName()));
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 32;
        result = PRIME * getSubTaskName().length() * result;
        return result;
    }
}
