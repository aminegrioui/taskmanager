package com.aminejava.taskmanager.securityconfig.rolespermissions;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum ApplicationRoles {
    USER(Sets.newHashSet());

    private final HashSet<ApplicationPermission> applicationPermissions;

    ApplicationRoles(HashSet<ApplicationPermission> applicationPermissions1) {
        this.applicationPermissions = applicationPermissions1;
    }

    public Set<SimpleGrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authoritySet;
        authoritySet = applicationPermissions.stream().map(applicationPermission -> new SimpleGrantedAuthority(applicationPermission.name())).collect(Collectors.toSet());
        authoritySet.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authoritySet;
    }
}
