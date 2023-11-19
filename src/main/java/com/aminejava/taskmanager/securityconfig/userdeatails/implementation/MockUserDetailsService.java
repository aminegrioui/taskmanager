package com.aminejava.taskmanager.securityconfig.userdeatails.implementation;

import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles;
import com.aminejava.taskmanager.securityconfig.userdeatails.models.ApplicationUserDetails;
import com.aminejava.taskmanager.securityconfig.userdeatails.IApplicationUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service("mock")
public class MockUserDetailsService implements IApplicationUserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUser(String userName) {
        return allUsers().stream().filter(myUserDetails -> myUserDetails.getUsername().equals(userName)).findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Not Found: " + userName));
    }

    @Override
    public UserDetails loadAdmin(String username) {
        return null;
    }

    public List<UserDetails> allUsers() {

        return Arrays.asList(

                new ApplicationUserDetails("user", passwordEncoder.encode("password"), ApplicationRoles.USER.getAuthorities(),
                        true, true, true, true,1l)


        );
    }

}
