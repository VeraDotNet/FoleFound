package com.veradotnet.folefound.campus.domain.service;

import com.veradotnet.folefound.campus.application.dto.CampusDTO;
import com.veradotnet.folefound.campus.application.mapper.CampusMapper;
import com.veradotnet.folefound.campus.domain.model.Campus;
import com.veradotnet.folefound.campus.domain.repository.CampusRepo;
import com.veradotnet.folefound.shared.exception.RessourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampusService {

    private final CampusRepo campusRepo;

    public CampusDTO saveCampus(CampusDTO campusDTO) {
        //conversion du campusDto envoyé par postman en entité campus
        Campus campusToSave = CampusMapper.INSTANCE.toModel(campusDTO);

        //enregistre dans la BD
        Campus persistedCampus = campusRepo.save(campusToSave);

        //transforme l'entité en dto pour la sortie à postman
        return CampusMapper.INSTANCE.toDTO(persistedCampus);
    }

    public List<CampusDTO> getCampuses(){
        List<Campus> campuses = campusRepo.findAll();

        //Conversion de toute la liste d'entité en dtos
        return campuses.stream()
                .map(campus -> CampusMapper.INSTANCE.toDTO(campus))
                .toList();
    }

    public CampusDTO getCampus(Long id) throws RessourceNotFoundException {
        Optional<Campus> optionalCampus = campusRepo.findById(id);
        return optionalCampus
                .map(campus -> CampusMapper.INSTANCE.toDTO(campus))
                .orElseThrow(() -> new RessourceNotFoundException("Campus not found"));
    }

    public CampusDTO updateCampus(Long id, CampusDTO campusDTO) throws RessourceNotFoundException {
        Campus campusToUpdate = campusRepo.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Campus not found"));

        //modification du nom via le dto
        campusToUpdate.setName(campusDTO.getName());

        //saving in DB
        Campus updatedCampus = campusRepo.save(campusToUpdate);

        //convert into dto to display
        return CampusMapper.INSTANCE.toDTO(updatedCampus);
    }

    public boolean deleteCampus(Long id) throws RessourceNotFoundException {
        Campus campusToDelete = campusRepo.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Campus not found"));

        //Soft delete
        campusToDelete.setIsActive(false);
        campusRepo.save(campusToDelete);
        return true;
    }
}
