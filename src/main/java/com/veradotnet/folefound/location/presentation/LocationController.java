package com.veradotnet.folefound.location.presentation;

import com.veradotnet.folefound.location.application.dto.LocationDTO;
import com.veradotnet.folefound.location.domain.service.LocationService;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO locationDTO) throws ResourceNotFoundException {
        return new ResponseEntity<>(locationService.saveLocation(locationDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LocationDTO>> getLocations(){
        return new ResponseEntity<>(locationService.getLocations(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocation(@PathVariable("id") Long id) throws ResourceNotFoundException {
        return new ResponseEntity<>(locationService.getLocation(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable("id") Long id, @Valid @RequestBody LocationDTO locationDTO) throws ResourceNotFoundException {
        return new ResponseEntity<>(locationService.updateLocation(id, locationDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteLocation(@PathVariable("id") Long id) throws ResourceNotFoundException {
        return new ResponseEntity<>(locationService.deleteLocation(id), HttpStatus.OK);
    }
}
