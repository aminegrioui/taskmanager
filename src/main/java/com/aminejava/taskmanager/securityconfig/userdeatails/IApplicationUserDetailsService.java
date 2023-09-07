package com.aminejava.taskmanager.securityconfig.userdeatails;

import org.springframework.security.core.userdetails.UserDetails;

public interface IApplicationUserDetailsService {
    UserDetails loadUser(String username);
}
