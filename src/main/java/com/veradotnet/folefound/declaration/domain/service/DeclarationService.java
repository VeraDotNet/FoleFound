package com.veradotnet.folefound.declaration.domain.service;

import com.veradotnet.folefound.category.domain.model.Category;
import com.veradotnet.folefound.category.domain.repository.CategoryRepo;
import com.veradotnet.folefound.declaration.application.dto.DeclarationRequestDTO;
import com.veradotnet.folefound.declaration.application.dto.DeclarationResponseDTO;
import com.veradotnet.folefound.declaration.application.enums.DeclarationStatus;
import com.veradotnet.folefound.declaration.application.enums.DeclarationType;
import com.veradotnet.folefound.declaration.application.mapper.DeclarationMapper;
import com.veradotnet.folefound.declaration.domain.model.Declaration;
import com.veradotnet.folefound.declaration.domain.repository.DeclarationRepo;
//import com.veradotnet.folefound.image.domain.model.Image;
import com.veradotnet.folefound.image.domain.service.S3Service;
import com.veradotnet.folefound.item.application.enums.ItemState;
import com.veradotnet.folefound.item.domain.Model.Item;
import com.veradotnet.folefound.location.application.dto.LocationDTO;
import com.veradotnet.folefound.location.application.mapper.LocationMapper;
import com.veradotnet.folefound.location.domain.model.Location;
import com.veradotnet.folefound.location.domain.repository.LocationRepo;
import com.veradotnet.folefound.matching.domain.service.MatchingService;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import com.veradotnet.folefound.users.domain.model.Users;
import com.veradotnet.folefound.users.domain.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeclarationService {

    private final CategoryRepo categoryRepo;

    private final LocationRepo locationRepo;

    private final UserRepo userRepo;

    //private final S3Service s3Service;

    private final DeclarationRepo declarationRepo;

    private final QRCodeService qrCodeService;

    private final MatchingService matchingService;

    @Transactional
    public DeclarationResponseDTO saveDeclaration(DeclarationRequestDTO dto, Long currentUserId) throws ResourceNotFoundException {

        // 1. Récupération des entités parentes obligatoires
        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
        Location location = locationRepo.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Lieu introuvable"));
        Users user = userRepo.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        // 2. Construction de l'objet physique (Item) via MapStruct
        Item item = DeclarationMapper.INSTANCE.toItemModel(dto);
        item.setName(item.getName().trim());
        item.setColor(item.getColor().trim().toLowerCase());
        item.setCategory(category);
        item.setItemState(dto.getType() == DeclarationType.LOST ? ItemState.LOST : ItemState.FOUND);

        // 3. Construction de la Déclaration
        Declaration declaration = DeclarationMapper.INSTANCE.toDeclarationModel(dto);
        declaration.setLocation(location);
        declaration.setUser(user);
        declaration.setItem(item);
        declaration.setStatus(DeclarationStatus.ACTIVE);
        declaration.setDateEvent(dto.getDateEvent());

        if (declaration.getType() == DeclarationType.FOUND) {
            // Génère un identifiant unique pour le QR Code (Ex: QR-FOUND-1719924823)
            String uniquePayload = "QR-FOUND-" + System.currentTimeMillis();
            declaration.setQrCode(uniquePayload);
        } else {
            declaration.setQrCode(null); // Reste null pour un objet perdu (LOST)
        }

        // 4. Sauvegarde globale
        Declaration savedDeclaration = declarationRepo.save(declaration);

        matchingService.triggerMatching(savedDeclaration);

        return DeclarationMapper.INSTANCE.toResponseDTO(savedDeclaration);
    }

    @Transactional
    public DeclarationResponseDTO getDeclaration(Long id) throws ResourceNotFoundException {
        Declaration declaration = declarationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Déclaration not found"));

        // Transformation en DTO de réponse
        DeclarationResponseDTO responseDTO = DeclarationMapper.INSTANCE.toResponseDTO(declaration);

        if (declaration.getType() == DeclarationType.FOUND && declaration.getQrCode() != null) {
            // On transforme le texte brut de la BDD en image Base64 pour le DTO
            String base64Image = qrCodeService.generateQrCodeBase64(declaration.getQrCode(), 250, 250);
            responseDTO.setQrCode(base64Image); // Le DTO transporte l'image prête pour le Front React
        }

        return responseDTO;
    }

    public Page<LocationDTO> getLocations(Pageable pageable){
        //get all the list in DB
        Page<Location> locations = locationRepo.findAll(pageable);

        //conversion en dtos et display
        return locations
                .map(location -> LocationMapper.INSTANCE.toDTO(location));
    }

    public Page<DeclarationResponseDTO> getDeclarationsForAgent(Pageable pageable) {

        Page<Declaration> declarations = declarationRepo.findAll(pageable);

        return declarations
                .map(DeclarationMapper.INSTANCE::toResponseDTO);
    }

    @Transactional
    public Page<DeclarationResponseDTO> getDeclarationsByUserId(Long userId, Pageable pageable) {

        Page<Declaration> declarationsByUserId = declarationRepo.findByUserId(userId, pageable);

        return declarationsByUserId
                .map(DeclarationMapper.INSTANCE::toResponseDTO);
    }

    @Transactional
    public DeclarationResponseDTO updateDeclaration(Long id, DeclarationRequestDTO dto) throws ResourceNotFoundException {
        Declaration declaration = declarationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Déclaration not found"));

        // Sécurité : On ne modifie pas une déclaration résolue
        if (declaration.getStatus() == DeclarationStatus.RESOLVED) {
            throw new IllegalStateException("Cannot modify a resolved declaration");
        }

        // Mise à jour des informations de l'Item lié
        Item item = declaration.getItem();
        item.setName(dto.getItemName().trim());
        item.setColor(dto.getColor().trim().toLowerCase());

        if (!item.getCategory().getId().equals(dto.getCategoryId())) {
            Category newCategory = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(newCategory);
        }

        // Mise à jour des informations de la Déclaration
        if (!declaration.getLocation().getId().equals(dto.getLocationId())) {
            Location newLocation = locationRepo.findById(dto.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
            declaration.setLocation(newLocation);
        }

        declaration.setDateEvent(dto.getDateEvent());
        // Note: On ne change pas le TYPE (LOST/FOUND) ni le qrCode initial pour des raisons de cohérence de l'inventaire

        Declaration updated = declarationRepo.save(declaration);
        return DeclarationMapper.INSTANCE.toResponseDTO(updated);
    }

    /*@Transactional
    public void deleteDeclaration(Long id) throws ResourceNotFoundException {
        Declaration declaration = declarationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Déclaration introuvable"));

        // Option A : Suppression physique de la BDD
        //declarationRepo.delete(declaration);

        // Option B Soft Delete / Passer le statut à ARCHIVED
         declaration.setStatus(DeclarationStatus.ARCHIVED);
         declarationRepo.save(declaration);
    }*/

    @Transactional
    public DeclarationResponseDTO archiveDeclaration(Long declarationId) throws ResourceNotFoundException {
        // 1. Récupérer la déclaration
        Declaration declaration = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new ResourceNotFoundException("Declaration not found"));

        // 2. Mettre à jour le statut de la déclaration
        declaration.setStatus(DeclarationStatus.ARCHIVED);

        // 3. Mettre à jour l'objet physique lié (s'il existe)
        if (declaration.getItem() != null) {
            declaration.getItem().setItemState(ItemState.ARCHIVED);
        }

        // 4. Sauvegarder et retourner le DTO
        Declaration archivedDeclaration = declarationRepo.save(declaration);
        return DeclarationMapper.INSTANCE.toResponseDTO(archivedDeclaration);
    }

    public Page<DeclarationResponseDTO> getDeclarationsWithGeneralFilters(
            Long categoryId, Long campusId, Long locationId, String statusStr, DeclarationType type, LocalDateTime startDate, Pageable pageable) {

        DeclarationStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = DeclarationStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Statut invalide passé, on l'ignore pour ne pas bloquer la requête
            }
        }

        Page<Declaration> declarationPage = declarationRepo.searchDeclarationsGeneral(
                categoryId,
                campusId,
                locationId,
                status,
                type,
                startDate,
                pageable
        );

        return declarationPage.map(DeclarationMapper.INSTANCE::toResponseDTO);
    }
}
