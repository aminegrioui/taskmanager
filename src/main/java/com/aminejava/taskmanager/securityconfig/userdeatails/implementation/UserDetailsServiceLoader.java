package com.aminejava.taskmanager.securityconfig.userdeatails.implementation;

import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.userdeatails.ApplicationUserDetails;
import com.aminejava.taskmanager.securityconfig.userdeatails.IApplicationUserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service("mysqldb")
public class UserDetailsServiceLoader implements IApplicationUserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUser(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        optionalUser.orElseThrow(() -> new UsernameNotFoundException("This user with this username: " + username + " is not found "));
        User user = optionalUser.get();

        return new ApplicationUserDetails(username,
                user.getPassword(),
                user.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toSet()),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                user.isEnabled()


        );
    }
}
