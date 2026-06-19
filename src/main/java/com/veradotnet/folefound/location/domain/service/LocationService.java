package com.veradotnet.folefound.location.domain.service;

import com.veradotnet.folefound.location.application.dto.LocationDTO;
import com.veradotnet.folefound.location.application.mapper.LocationMapper;
import com.veradotnet.folefound.location.domain.model.Location;
import com.veradotnet.folefound.location.domain.repository.LocationRepo;
import com.veradotnet.folefound.shared.exception.RessourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepo locationRepo;

    public LocationDTO saveLocation(LocationDTO locationDTO){
        //conversion du dto en model
        Location locationToSave = LocationMapper.INSTANCE.toModel(locationDTO);

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

    public LocationDTO getLocation(Long id) throws RessourceNotFoundException{
        Optional<Location> optionalLocation = locationRepo.findById(id);
        return optionalLocation
                .map(location -> LocationMapper.INSTANCE.toDTO(location))
                .orElseThrow(() -> new RessourceNotFoundException("Location not found"));
    }

    public LocationDTO updateLocation(Long id, LocationDTO locationDTO) throws RessourceNotFoundException{
        //find the location(model) to update
        Location locationToUpdate = locationRepo.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Location not found"));

        //update the name
        locationToUpdate.setName(locationDTO.getName());

        //save in DB
        Location updatedLocation = locationRepo.save(locationToUpdate);

        //convert in dto then display
        return LocationMapper.INSTANCE.toDTO(updatedLocation);
    }

    public Boolean deleteLocation(Long id) throws RessourceNotFoundException {
        //find  the location to delete
        Location locationToDelete = locationRepo.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Campus not found"));

        //set the isActive field to false (soft delete)
        locationToDelete.setIsActive(false);

        //save in db
        locationRepo.save(locationToDelete);
        return true;
    }
}
