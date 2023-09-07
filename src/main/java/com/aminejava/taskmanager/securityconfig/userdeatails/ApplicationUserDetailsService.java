package com.aminejava.taskmanager.securityconfig.userdeatails;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final IApplicationUserDetailsService iApplicationUserDetailsService;

    public ApplicationUserDetailsService(@Qualifier("mysqldb") IApplicationUserDetailsService iApplicationUserDetailsService) {
        this.iApplicationUserDetailsService = iApplicationUserDetailsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = iApplicationUserDetailsService.loadUser(username);
        return user;
    }
}
