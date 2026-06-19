package com.veradotnet.folefound.location.application.mapper;

import com.veradotnet.folefound.location.application.dto.LocationDTO;
import com.veradotnet.folefound.location.domain.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LocationMapper {
    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    @Mapping(source = "campus.id", target = "campusId")
    LocationDTO toDTO(Location location);

    @Mapping(target = "campus", ignore = true)
    Location toModel(LocationDTO locationDTO);
}
