package com.veradotnet.folefound.users.application.mapper;

import com.veradotnet.folefound.users.application.dto.UserDTO;
import com.veradotnet.folefound.users.domain.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StudentMapper {
    StudentMapper INSTANCE = Mappers.getMapper(StudentMapper.class);

    Student toModel(UserDTO userDTO);

    UserDTO toDTO(Student student);
}
