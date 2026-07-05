package com.veradotnet.folefound.item.application.mapper;

import com.veradotnet.folefound.item.application.dto.ItemDTO;
import com.veradotnet.folefound.item.domain.Model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ItemMapper {
    ItemMapper INSTANCE = Mappers.getMapper(ItemMapper.class);

    @Mapping(source = "category.id", target = "categoryId")
    ItemDTO toDTO(Item item);

    @Mapping(target = "category", ignore = true)
    Item toModel(ItemDTO itemDTO);
}
