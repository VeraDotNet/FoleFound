package com.veradotnet.folefound.location.application.mapper;

import com.veradotnet.folefound.location.application.dto.LocationDTO;
import com.veradotnet.folefound.location.domain.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LocationMapper {
    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);

    LocationDTO toDTO(Location location);

    Location toModel(LocationDTO locationDTO);
}
