package com.analyfy.analify.Mapper;





import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.analyfy.analify.DTO.AdminGeneralDTO;
import com.analyfy.analify.Entity.AdminGeneral;


@Component
@Mapper(componentModel="spring")
public interface AdminGeneralMapper extends BaseMapper<AdminGeneralDTO, AdminGeneral> {
    
}
