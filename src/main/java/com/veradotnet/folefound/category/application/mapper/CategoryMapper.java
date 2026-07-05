package com.veradotnet.folefound.category.application.mapper;

import com.veradotnet.folefound.category.application.dto.CategoryDTO;
import com.veradotnet.folefound.category.domain.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryDTO toDTO(Category category);

    Category toModel(CategoryDTO categoryDTO);
}
