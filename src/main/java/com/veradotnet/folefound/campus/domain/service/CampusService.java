package com.veradotnet.folefound.campus.domain.service;

import com.veradotnet.folefound.campus.application.dto.CampusDTO;
import com.veradotnet.folefound.campus.application.mapper.CampusMapper;
import com.veradotnet.folefound.campus.domain.model.Campus;
import com.veradotnet.folefound.campus.domain.repository.CampusRepo;
import com.veradotnet.folefound.location.domain.repository.LocationRepo;
import com.veradotnet.folefound.shared.exception.ResourceInUseException;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampusService {

    private final CampusRepo campusRepo;

    private final LocationRepo locationRepo;

    public CampusDTO saveCampus(CampusDTO campusDTO) {
        //conversion du campusDto envoyé par postman en entité campus
        Campus campusToSave = CampusMapper.INSTANCE.toModel(campusDTO);

        //enregistre dans la BD
        Campus persistedCampus = campusRepo.save(campusToSave);

        //transforme l'entité en dto pour la sortie à postman
        return CampusMapper.INSTANCE.toDTO(persistedCampus);
    }

    @Transactional(readOnly = true)
    public Page<CampusDTO> getCampuses(Pageable pageable){
        Page<Campus> campuses = campusRepo.findAll(pageable);

        //Conversion de toute la liste d'entité en dtos
        return campuses
                .map(campus -> CampusMapper.INSTANCE.toDTO(campus));
    }

    public CampusDTO getCampus(Long id) throws ResourceNotFoundException {
        Optional<Campus> optionalCampus = campusRepo.findById(id);
        return optionalCampus
                .map(campus -> CampusMapper.INSTANCE.toDTO(campus))
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));
    }

    public CampusDTO updateCampus(Long id, CampusDTO campusDTO) throws ResourceNotFoundException {
        Campus campusToUpdate = campusRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        //modification du nom via le dto
        campusToUpdate.setName(campusDTO.getName());

        //saving in DB
        Campus updatedCampus = campusRepo.save(campusToUpdate);

        //convert into dto to display
        return CampusMapper.INSTANCE.toDTO(updatedCampus);
    }

    public boolean deleteCampus(Long id) throws ResourceNotFoundException {
        Campus campusToDelete = campusRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        boolean hasLocations = locationRepo.existsByCampusId(id);
        if (hasLocations) {
            throw new ResourceInUseException("Cannot delete this campus because it has locations associated with it. Please delete or reassign the locations first.");
        }

        campusRepo.delete(campusToDelete);
        return true;
    }
}
