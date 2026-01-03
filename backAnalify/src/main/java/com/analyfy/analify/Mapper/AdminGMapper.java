package com.analyfy.analify.Mapper;





import org.mapstruct.Mapper;

import com.analyfy.analify.DTO.AdminGDTO;
import com.analyfy.analify.Entity.AdminG;


@Mapper(componentModel = "spring")
public interface AdminGMapper extends BaseMapper<AdminGDTO, AdminG> {}
