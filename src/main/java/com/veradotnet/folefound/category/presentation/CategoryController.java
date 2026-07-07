package com.veradotnet.folefound.category.presentation;

import com.veradotnet.folefound.category.application.dto.CategoryDTO;
import com.veradotnet.folefound.category.domain.service.CategoryService;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDTO> saveCategory(@Valid @RequestBody CategoryDTO categoryDTO ){
        return new ResponseEntity<>(categoryService.saveCategory(categoryDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> getCategories(
            @ParameterObject @PageableDefault(page = 0, size = 5, sort = "name", direction = Sort.Direction.ASC) Pageable pageable){
        return new ResponseEntity<>(categoryService.getCategories(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable("id") Long id) throws ResourceNotFoundException {
        return new ResponseEntity<>(categoryService.getCategory(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable("id") Long id, @Valid @RequestBody CategoryDTO categoryDTO) throws ResourceNotFoundException {
        return new ResponseEntity<>(categoryService.updateCategory(id, categoryDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCategory(@PathVariable("id") Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CategoryDTO>> searchCategories(
            @RequestParam String name,
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<CategoryDTO> results = categoryService.searchCategoriesByName(name, pageable);
        return ResponseEntity.ok(results);
    }
}
