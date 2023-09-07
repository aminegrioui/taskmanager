package com.aminejava.taskmanager.securityconfig;

import com.aminejava.taskmanager.securityconfig.jwt.filter.JwtTokenFilter;
import com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles;
import com.aminejava.taskmanager.securityconfig.userdeatails.ApplicationUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfigSpringSecurity extends WebSecurityConfigurerAdapter {

    private final ApplicationUserDetailsService applicationUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenFilter jwtTokenFilter;

    public ConfigSpringSecurity(ApplicationUserDetailsService applicationUserDetailsService, PasswordEncoder passwordEncoder, JwtTokenFilter jwtTokenFilter) {
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
                .antMatchers("/taskmanager/v1/user/**").hasRole(ApplicationRoles.USER.name())
                .antMatchers(("/taskmanager/v1/auth/**")).permitAll()
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
