package com.veradotnet.folefound.users.application.mapper;

import com.veradotnet.folefound.users.application.dto.UserDTO;
import com.veradotnet.folefound.users.domain.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDTO toDTO(Users user);

    Users toModel(UserDTO userDTO);
}
