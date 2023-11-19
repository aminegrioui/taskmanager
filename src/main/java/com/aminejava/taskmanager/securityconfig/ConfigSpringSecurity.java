package com.aminejava.taskmanager.securityconfig;

import com.aminejava.taskmanager.securityconfig.jwt.filter.JwtTokenFilter;
import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationPermission;
import com.aminejava.taskmanager.securityconfig.userdeatails.ApplicationDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationPermission.*;
import static com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles.*;

@Configuration
@EnableWebSecurity
public class ConfigSpringSecurity extends WebSecurityConfigurerAdapter {

    private final ApplicationDetailsService applicationUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenFilter jwtTokenFilter;

    public ConfigSpringSecurity(ApplicationDetailsService applicationUserDetailsService, PasswordEncoder passwordEncoder, JwtTokenFilter jwtTokenFilter) {
        this.applicationUserDetailsService = applicationUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()

                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterAfter(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()


                // management config: SUPER_ADMIN
                .antMatchers("/taskmanager/v1/superadmin/management/addManagementRole").hasAnyAuthority(WRITE_SUPER_ADMIN.getName(),WRITE_ADMIN.getName(), WRITE_MANAGER.getName())
                .antMatchers("/taskmanager/v1/superadmin/management/disableUser").hasAnyAuthority(DISABLE_USER.getName())
                .antMatchers("/taskmanager/v1/superadmin/management/deleteSuperAdmin/**").hasAnyAuthority(WRITE_SUPER_ADMIN.getName())
                .antMatchers("/taskmanager/v1/superadmin/management/deleteUser/**").hasAnyAuthority(WRITE_USER.getName())
                .antMatchers("/taskmanager/v1/superadmin/management/deleteManagerRole/**").hasAnyAuthority(WRITE_ADMIN.getName(), WRITE_MANAGER.getName())
                .antMatchers("/taskmanager/v1/superadmin/management/affectManagerRole","/taskmanager/v1/superadmin/management/affectPermission")
                .hasAnyAuthority(AFFECT_ROLE_PERMISSION.getName())
                .antMatchers("taskmanager/v1/superadmin/profile/**").hasRole(SUPER_ADMIN.name())
                .antMatchers(HttpMethod.GET, "/taskmanager/v1/superadmin/management/**").hasRole(SUPER_ADMIN.name())

                // Admin
                .antMatchers(HttpMethod.POST, "/taskmanager/v1/admin/management/addManager/**").hasAuthority(WRITE_MANAGER.getName())
                .antMatchers(HttpMethod.POST, "/taskmanager/v1/admin/management/saveUser/**").hasAuthority(WRITE_USER.getName())
                .antMatchers(HttpMethod.DELETE, "/taskmanager/v1/admin/management/deleteManager/**").hasAnyAuthority(WRITE_MANAGER.getName())
                .antMatchers(HttpMethod.DELETE, "/taskmanager/v1/admin/management/deleteUser/**").hasAnyAuthority(WRITE_USER.getName())
                .antMatchers(HttpMethod.GET, "/taskmanager/v1/admin/management/**").hasRole(ADMIN.name())

                // Manager
                .antMatchers(HttpMethod.POST, "/taskmanager/v1/manager/management/**").hasAuthority(WRITE_PROJECT.getName())
                .antMatchers(HttpMethod.GET, "/taskmanager/v1/manager/management/**").hasRole(MANAGER.name())

                // user config
                .antMatchers("/taskmanager/v1/user/**",
                        "/taskmanager/v1/projects/**",
                        "/taskmanager/v1/tasks/**",
                        "/taskmanager/v1/subTasks/**").hasRole(USER.name())

                // InterService Register Super Admin. It should be in separate Project
                .antMatchers("/taskmanager/v1/intern/management/**").permitAll()

                .antMatchers(("/taskmanager/v1/auth/**")).permitAll()
                .antMatchers("/taskmanager/v1/authadmin/**").permitAll()
                .anyRequest().authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(applicationUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
