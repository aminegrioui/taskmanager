package com.aminejava.taskmanager.dto.user;

import com.aminejava.taskmanager.dto.project.ProjectResponseDto;
import com.aminejava.taskmanager.model.UserDetails;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
public class UserWithAllDetailsResponseDtoForGetAll {

    private UserDetails userDetails;

    private String username;

    private String role;

    private boolean isAccountNonLocked;

    private boolean isCredentialsNonExpired;

    private boolean isAccountNonExpired;

    private String email;

    private Set<ProjectResponseDto> projectResponseDtos;

    private String usernameAdmin;

    private ZonedDateTime zonedDateTimeLockedUser;

    public UserWithAllDetailsResponseDtoForGetAll(UserDetails userDetails, String username, String role,
                                                  boolean isAccountNonLocked, boolean isCredentialsNonExpired,
                                                  boolean isAccountNonExpired, String email, Set<ProjectResponseDto> projectResponseDtos,
                                                  String usernameAdmin, ZonedDateTime zonedDateTimeLockedUser) {
        this.userDetails = userDetails;
        this.username = username;
        this.role = role;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isAccountNonExpired = isAccountNonExpired;
        this.email = email;
        this.projectResponseDtos = projectResponseDtos;
        this.usernameAdmin = usernameAdmin;
        this.zonedDateTimeLockedUser = zonedDateTimeLockedUser;
    }
}
