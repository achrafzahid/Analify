package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.UserDTO;
import com.analyfy.analify.Entity.User;


@Component
@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<UserDTO, User>{
    
}
