package com.veradotnet.folefound.category.domain.service;

import com.veradotnet.folefound.category.application.dto.CategoryDTO;
import com.veradotnet.folefound.category.application.mapper.CategoryMapper;
import com.veradotnet.folefound.category.domain.model.Category;
import com.veradotnet.folefound.category.domain.repository.CategoryRepo;
import com.veradotnet.folefound.item.domain.repository.ItemRepo;
import com.veradotnet.folefound.shared.exception.ResourceInUseException;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepo categoryRepo;

    private final ItemRepo itemRepo;

    @Transactional
    public CategoryDTO saveCategory(CategoryDTO categoryDTO ) {
        if (categoryRepo.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new IllegalArgumentException("This category already exists.");
        }
        //conversion du campusDto envoyé par postman en entité campus
        Category categoryToSave = CategoryMapper.INSTANCE.toModel(categoryDTO);

        //enregistre dans la BD
        Category persistedCategory = categoryRepo.save(categoryToSave);

        //transforme l'entité en dto pour la sortie à postman
        return CategoryMapper.INSTANCE.toDTO(persistedCategory);
    }

    public Page<CategoryDTO> getCategories(Pageable pageable){
        Page<Category> categories = categoryRepo.findAll(pageable);

        //Conversion de toute la liste d'entité en dtos
        return categories
                .map(category -> CategoryMapper.INSTANCE.toDTO(category));
    }

    public CategoryDTO getCategory(Long id) throws ResourceNotFoundException {
        Optional<Category> optionalCategory = categoryRepo.findById(id);
        return optionalCategory
                .map(category -> CategoryMapper.INSTANCE.toDTO(category))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) throws ResourceNotFoundException {
        Category categoryToUpdate = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        //modification du nom via le dto
        categoryToUpdate.setName(categoryDTO.getName());

        //saving in DB
        Category updatedCategory = categoryRepo.save(categoryToUpdate);

        //convert into dto to display
        return CategoryMapper.INSTANCE.toDTO(updatedCategory);
    }

    @Transactional
    public boolean deleteCategory(Long id) throws ResourceNotFoundException {
        Category categoryToDelete = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        boolean hasItems = itemRepo.existsByCategoryId(id);
        if (hasItems) {
            throw new ResourceInUseException("Cannot delete this category because it has items associated with it. Please delete or reassign the items first.");
        }

        categoryRepo.delete(categoryToDelete);
        return true;
    }

    public Page<CategoryDTO> searchCategoriesByName(String name, Pageable pageable) {
        Page<Category> categories = categoryRepo.findByNameContainingIgnoreCase(name, pageable);
        return categories.map(CategoryMapper.INSTANCE::toDTO);
    }
}
