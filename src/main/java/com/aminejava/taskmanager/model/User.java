package com.aminejava.taskmanager.model;

import com.aminejava.taskmanager.model.admin.Admin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private boolean isEnabled;

    private String role;

    private boolean isAccountNonLocked;

    private boolean isCredentialsNonExpired;

    private boolean isAccountNonExpired;

    private String email;

    @JsonIgnore
    private boolean deleted;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @ManyToMany
    @JoinTable(
            name = "user_project_manager",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "project_manager_id"))
    private Set<ProjectManager> projectManagerList;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_details_id")
    private UserDetails userDetails;

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

        User user = (User) o;

        return (getUsername() != null && Objects.equals(getUsername(), user.getUsername()));
    }

    public User(String username) {
        this.username = username;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 32;
        result = PRIME * getUsername().length() * result;
        return result;
    }

    private ZonedDateTime zonedDateTimeLockedUser;
}
