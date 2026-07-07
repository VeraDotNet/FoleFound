package com.veradotnet.folefound.restitution.application.mapper;

import com.veradotnet.folefound.restitution.application.dto.RestitutionDTO;
import com.veradotnet.folefound.restitution.domain.model.Restitution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RestitutionMapper {

    RestitutionMapper INSTANCE = Mappers.getMapper(RestitutionMapper.class);

    @Mapping(source = "matching.id", target = "matchingId")
    @Mapping(source = "agent.id", target = "agentId")
    @Mapping(source = "agent.firstName", target = "agentName")
    @Mapping(source = "matching.foundDeclaration.id", target = "foundDeclarationId")
    @Mapping(source = "matching.foundDeclaration.item.name", target = "itemName")
    @Mapping(source = "matching.foundDeclaration.item.category.name", target = "categoryName")
    RestitutionDTO toDTO(Restitution restitution);

    @Mapping(target = "matching", ignore = true)
    @Mapping(target = "agent", ignore = true)
    Restitution toModel(RestitutionDTO restitutionDTO);
}
