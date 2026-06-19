package com.veradotnet.folefound.campus.application.mapper;

import com.veradotnet.folefound.campus.application.dto.CampusDTO;
import com.veradotnet.folefound.campus.domain.model.Campus;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CampusMapper {
    CampusMapper INSTANCE = Mappers.getMapper(CampusMapper.class);

    CampusDTO toDTO(Campus campus);

    Campus toModel(CampusDTO campusDTO);
}
