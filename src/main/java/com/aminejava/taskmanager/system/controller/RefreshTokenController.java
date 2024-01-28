package com.aminejava.taskmanager.system.controller;

import com.aminejava.taskmanager.dto.user.LoginResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/taskmanager/v1/refreshToken")
public class RefreshTokenController {


    @GetMapping
    public LoginResponseDto refreshToken(HttpServletResponse response) {
        LoginResponseDto loginResponseDto=new LoginResponseDto();
        loginResponseDto.setJwtRefreshToken(response.getHeader("refreshToken"));
        loginResponseDto.setJwtAccessToken(response.getHeader("accessToken"));
        return loginResponseDto;
    }
}
