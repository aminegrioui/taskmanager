package com.aminejava.taskmanager.securityconfig;

import com.aminejava.taskmanager.securityconfig.jwt.filter.JwtTokenFilter;
import com.aminejava.taskmanager.securityconfig.userdeatails.ApplicationDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfigSpringSecurity {

    private final ApplicationDetailsService applicationUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenFilter jwtTokenFilter;

    public ConfigSpringSecurity(ApplicationDetailsService applicationUserDetailsService,
                                PasswordEncoder passwordEncoder,
                                JwtTokenFilter jwtTokenFilter) {
        this.applicationUserDetailsService = applicationUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and().csrf().disable()

                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterAfter(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()

                .requestMatchers("/taskmanager/v1/superadmin/management/**", "/taskmanager/v1/superadmin/profile/**").hasRole(SUPER_ADMIN.name())

                // Admin
                .requestMatchers("/taskmanager/v1/admin/profile/**",
                        "/taskmanager/v1/admin/management/**").hasRole(ADMIN.name())

                // Manager
                .requestMatchers("/taskmanager/v1/manager/management/**",
                        "/taskmanager/v1/manager/profile").hasRole(MANAGER.name())

                // user
                .requestMatchers("/taskmanager/v1/user/**",
                        "/taskmanager/v1/projects/**",
                        "/taskmanager/v1/tasks/**",
                        "/taskmanager/v1/subTasks/**").hasRole(USER.name())

                .requestMatchers("/taskmanager/v1/intern/management/**",
                        "/taskmanager/v1/auth/**",
                        "/taskmanager/v1/authadmin/**",
                        "/taskmanager/v1/refreshToken").permitAll()
                .requestMatchers("/taskmanager/v1/refreshToken").authenticated()
                .and().authenticationProvider(authenticationProvider());
        return httpSecurity.build();
    }

    protected AuthenticationProvider authenticationProvider() {
        return daoAuthenticationProvider();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(applicationUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
