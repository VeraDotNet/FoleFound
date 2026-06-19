package com.veradotnet.folefound.campus.presentation;

import com.veradotnet.folefound.campus.application.dto.CampusDTO;
import com.veradotnet.folefound.campus.domain.service.CampusService;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/campuses")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    @PostMapping
    public ResponseEntity<CampusDTO> saveCampus(@Valid @RequestBody CampusDTO campusDTO){
        return new ResponseEntity<>(campusService.saveCampus(campusDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CampusDTO>> getCampuses(){
        return new ResponseEntity<>(campusService.getCampuses(), HttpStatus.OK);
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
}
