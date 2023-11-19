package com.aminejava.taskmanager.dto.management.superadmin;

import com.aminejava.taskmanager.dto.management.AdminResponseDto;
import com.aminejava.taskmanager.dto.management.DeleteManagerRoleResponseDto;
import lombok.Data;

@Data
public class AffectRoleResponseDto {

    private AdminResponseDto adminResponseDto=new AdminResponseDto();
    private DeleteManagerRoleResponseDto deleteManagerRoleResponseDto=new DeleteManagerRoleResponseDto();
}
