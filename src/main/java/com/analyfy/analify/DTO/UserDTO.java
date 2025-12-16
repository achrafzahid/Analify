package com.analyfy.analify.DTO;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO extends EntityBaseDTO<Long> {
    private String name;
    private Date dateOfBirth;
}
