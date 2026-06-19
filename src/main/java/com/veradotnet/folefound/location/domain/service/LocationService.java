package com.veradotnet.folefound.location.domain.service;

import com.veradotnet.folefound.campus.domain.model.Campus;
import com.veradotnet.folefound.campus.domain.repository.CampusRepo;
import com.veradotnet.folefound.location.application.dto.LocationDTO;
import com.veradotnet.folefound.location.application.mapper.LocationMapper;
import com.veradotnet.folefound.location.domain.model.Location;
import com.veradotnet.folefound.location.domain.repository.LocationRepo;
import com.veradotnet.folefound.shared.exception.ResourceInUseException;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepo locationRepo;

    private final CampusRepo campusRepo;

    public LocationDTO saveLocation(LocationDTO locationDTO) throws ResourceNotFoundException {

        Campus campus = campusRepo.findById(locationDTO.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campus not found" + locationDTO.getCampusId()));

        //conversion du dto en model
        Location locationToSave = LocationMapper.INSTANCE.toModel(locationDTO);

        //Associer manuellement le vrai campus récupéré
        locationToSave.setCampus(campus);

        //enregistrement en BD
        Location persistedLocation = locationRepo.save(locationToSave);

        //display du model converti en dto
        return LocationMapper.INSTANCE.toDTO(persistedLocation);
    }

    public List<LocationDTO> getLocations(){
        //get all the list in DB
        List<Location> locations = locationRepo.findAll();

        //conversion en dtos et display
        return locations.stream()
                .map(location -> LocationMapper.INSTANCE.toDTO(location))
                .toList();
    }

    public LocationDTO getLocation(Long id) throws ResourceNotFoundException {
        Optional<Location> optionalLocation = locationRepo.findById(id);
        return optionalLocation
                .map(location -> LocationMapper.INSTANCE.toDTO(location))
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    public LocationDTO updateLocation(Long id, LocationDTO locationDTO) throws ResourceNotFoundException {
        //find the location(model) to update
        Location locationToUpdate = locationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        //update the name
        locationToUpdate.setName(locationDTO.getName());

        //if we want to modify the campus where the location is
        if (locationDTO.getCampusId() != null) {
            Campus campus = campusRepo.findById(locationDTO.getCampusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Campus not found : " + locationDTO.getCampusId()));
            locationToUpdate.setCampus(campus);
        }

        //save in DB
        Location updatedLocation = locationRepo.save(locationToUpdate);

        //convert in dto then display
        return LocationMapper.INSTANCE.toDTO(updatedLocation);
    }

    public Boolean deleteLocation(Long id) throws ResourceNotFoundException {
        //find  the location to delete
        Location locationToDelete = locationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        /*boolean hasDeclarations = declarationRepo.existsByLocationId(id);
        if (declarationRepo.existsByLocationId(locationId)) {
            throw new ResourceInUseException("Cannot delete this location because it is linked to object declarations.");
        }*/

        //save in db
        locationRepo.save(locationToDelete);
        return true;
    }
}
