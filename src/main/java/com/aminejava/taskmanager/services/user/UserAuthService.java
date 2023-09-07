package com.aminejava.taskmanager.services.user;

import com.aminejava.taskmanager.dto.user.UserLoginDto;
import com.aminejava.taskmanager.dto.user.UserRegisterDto;
import com.aminejava.taskmanager.model.Permission;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.repository.PermissionRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final PermissionRepository permissionRepository;

    public UserAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtGenerator jwtGenerator, PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
        this.permissionRepository = permissionRepository;
    }

    public String registerUser(UserRegisterDto userDto) {
        Optional<User> optionalUser = userRepository.findByUsername(userDto.getUsername());
        if (optionalUser.isEmpty()) {
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setEmail(userDto.getEmail());
            user.setEnabled(true);
            user.setCredentialsNonExpired(true);
            user.setAccountNonLocked(true);
            user.setAccountNonExpired(true);
            Permission permission = permissionRepository.findByPermission("ROLE_USER");
            user.getPermissions().add(permission);
            userRepository.save(user);
            return "User with userName " + userDto.getUsername() + " was created";
        }
        return "User with this userName: " + userDto.getUsername() + " was already existed ";
    }

    public ResponseEntity<?> login(UserLoginDto userLoginDto) throws Exception {
        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword());

            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("authorization", "Bearer " + jwtGenerator.generateJwtToken(authentication));
            return ResponseEntity.accepted().headers(httpHeaders).body("User with username: " + userLoginDto.getUsername() + " login successful");

        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect password or username ", e);
        }

    }
}
