package com.aminejava.taskmanager.model.admin;

import com.aminejava.taskmanager.model.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String username;

    private String password;

    private boolean isEnabled;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "admin_permissions",
            joinColumns = @JoinColumn(name = "admin_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    private boolean isAccountNonLocked;

    private boolean isCredentialsNonExpired;

    private boolean isAccountNonExpired;

    @JsonIgnore
    private String email;

    private boolean isDeleted;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_details_id")
    private UserDetails userDetails;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "admin")
    @JsonIgnore
    private Set<ProjectManager> projectManagers = new HashSet<>();


    @OneToMany(cascade = {CascadeType.REFRESH,CascadeType.MERGE,CascadeType.DETACH,CascadeType.PERSIST},   mappedBy = "admin")
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    private ZonedDateTime zonedDateTimeLockedUser;
    private long createdId;
    private String role;
}
