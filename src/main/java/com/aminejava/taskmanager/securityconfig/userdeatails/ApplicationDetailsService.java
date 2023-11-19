package com.aminejava.taskmanager.securityconfig.userdeatails;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class ApplicationDetailsService implements UserDetailsService {

    private final IApplicationUserDetailsService iApplicationUserDetailsService;

    public ApplicationDetailsService(@Qualifier("mysqldb") IApplicationUserDetailsService iApplicationUserDetailsService) {
        this.iApplicationUserDetailsService = iApplicationUserDetailsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws AuthenticationException {

        if (username.contains("ADMIN")) {
            return iApplicationUserDetailsService.loadAdmin(username);
        }
        return  iApplicationUserDetailsService.loadUser(username);
    }
}
