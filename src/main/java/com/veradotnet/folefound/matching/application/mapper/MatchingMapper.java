package com.veradotnet.folefound.matching.application.mapper;

import com.veradotnet.folefound.matching.application.dto.MatchingDTO;
import com.veradotnet.folefound.matching.domain.model.Matching;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MatchingMapper {

    MatchingMapper INSTANCE = Mappers.getMapper(MatchingMapper.class);

    @Mapping(source = "lostDeclaration.id", target = "lostDeclarationId")
    @Mapping(source = "lostDeclaration.item.name", target = "lostDeclarationItemName")
    @Mapping(source = "lostDeclaration.dateEvent", target = "lostDeclarationDate")
    @Mapping(source = "lostDeclaration.item.color", target = "lostDeclarationColor")
    @Mapping(source = "lostDeclaration.location.name", target = "lostDeclarationLocation")

    @Mapping(source = "foundDeclaration.id", target = "foundDeclarationId")
    @Mapping(source = "foundDeclaration.item.name", target = "foundDeclarationItemName")
    @Mapping(source = "foundDeclaration.dateEvent", target = "foundDeclarationDate")
    @Mapping(source = "foundDeclaration.item.color", target = "foundDeclarationColor")
    @Mapping(source = "foundDeclaration.location.name", target = "foundDeclarationLocation")

    @Mapping(source = "lostDeclaration.item.category.name", target = "categoryName")
    @Mapping(target = "matchingScore", ignore = true) // On dit à MapStruct de ne pas y toucher
    MatchingDTO toDTO(Matching matching);
}
