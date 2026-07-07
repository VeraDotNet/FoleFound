package com.veradotnet.folefound.campus.presentation;

import com.veradotnet.folefound.campus.application.dto.CampusDTO;
import com.veradotnet.folefound.campus.domain.service.CampusService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/campus")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    @PostMapping
    public ResponseEntity<CampusDTO> saveCampus(@Valid @RequestBody CampusDTO campusDTO){
        return new ResponseEntity<>(campusService.saveCampus(campusDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<CampusDTO>> getCampuses(
           @ParameterObject @PageableDefault(page = 0, size = 5, sort = "name", direction = Sort.Direction.ASC)Pageable pageable){
        return new ResponseEntity<>(campusService.getCampuses(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampusDTO> getCampus(@PathVariable("id") Long id) throws ResourceNotFoundException {
        return new ResponseEntity<>(campusService.getCampus(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampusDTO> updateCampus(@PathVariable("id") Long id, @Valid @RequestBody CampusDTO campusDTO) throws ResourceNotFoundException {
        return new ResponseEntity<>(campusService.updateCampus(id, campusDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCampus(@PathVariable("id") Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(campusService.deleteCampus(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<CampusDTO>> searchCampuses(
            @RequestParam String name,
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<CampusDTO> results = campusService.searchCampusesByName(name, pageable);
        return ResponseEntity.ok(results);
    }
}
