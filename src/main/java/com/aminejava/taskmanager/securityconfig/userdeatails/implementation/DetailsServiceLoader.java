package com.aminejava.taskmanager.securityconfig.userdeatails.implementation;

import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.userdeatails.models.ApplicationUserDetails;
import com.aminejava.taskmanager.securityconfig.userdeatails.IApplicationUserDetailsService;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles.USER;

@Service("mysqldb")
public class DetailsServiceLoader implements IApplicationUserDetailsService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public DetailsServiceLoader(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUser(String username) {

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty() || optionalUser.get().isDeleted()) {
            throw new InternalAuthenticationServiceException("User with this username is not found");
        }
        if(!optionalUser.get().isEnabled()){
            throw new InternalAuthenticationServiceException("The user is Disabaled. Contact Administartion");
        }

        User user = optionalUser.get();

        Collection<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + USER.name()));
        return new ApplicationUserDetails(username,
                user.getPassword(),
                grantedAuthorities,
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                user.isEnabled(),
                user.getId()
        );
    }

    @Override
    public UserDetails loadAdmin(String username) {
        String userNameAdmin = username.substring(0, username.indexOf("ADMIN"));
        Optional<Admin> optionalAdmin = adminRepository.findAdminByUsername(userNameAdmin);

        if (optionalAdmin.isEmpty() || optionalAdmin.get().isDeleted()) {
            throw new InternalAuthenticationServiceException("Admin with this username is not found");
        }
        if(!optionalAdmin.get().isEnabled()){
            throw new InternalAuthenticationServiceException("This Admin is Disabaled. Contact Administartion");
        }

        Admin admin = optionalAdmin.get();

        return new ApplicationUserDetails(username,
                admin.getPassword(),
                admin.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toSet()),
                admin.isAccountNonExpired(),
                admin.isAccountNonLocked(),
                admin.isCredentialsNonExpired(),
                admin.isEnabled(),
                admin.getAdminId()
        );
    }
}
