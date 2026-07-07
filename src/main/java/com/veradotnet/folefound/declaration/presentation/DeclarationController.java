package com.veradotnet.folefound.declaration.presentation;

import com.veradotnet.folefound.declaration.application.dto.DeclarationRequestDTO;
import com.veradotnet.folefound.declaration.application.dto.DeclarationResponseDTO;
import com.veradotnet.folefound.declaration.application.enums.DeclarationType;
import com.veradotnet.folefound.declaration.domain.service.DeclarationService;
import com.veradotnet.folefound.declaration.domain.service.QRCodeService;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import com.veradotnet.folefound.shared.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("api/v1/declaration")
@RequiredArgsConstructor
public class DeclarationController {

    private final DeclarationService declarationService;

    private final QRCodeService qrCodeService;

    @PostMapping
    public ResponseEntity<DeclarationResponseDTO> declare(@Valid @RequestBody DeclarationRequestDTO dto) throws ResourceNotFoundException {

        // Extrait dynamiquement l'ID de ton UserPrincipal via le Token décodé
        Long currentUserId = SecurityUtils.getCurrentUserId();

        DeclarationResponseDTO response = declarationService.saveDeclaration(dto, currentUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_AGENT') or @securityUtils.isDeclarationOwner(#id)")
    public ResponseEntity<DeclarationResponseDTO> getDeclarationById(@PathVariable Long id) throws ResourceNotFoundException {
        return new ResponseEntity<>(declarationService.getDeclaration(id), HttpStatus.OK);
    }

    @GetMapping("/my-declarations")
    public ResponseEntity<Page<DeclarationResponseDTO>> getMyDeclarations(
            @ParameterObject @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "dateCreated",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        Page<DeclarationResponseDTO> page = declarationService.getDeclarationsByUserId(currentUserId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping()
    public ResponseEntity<Page<DeclarationResponseDTO>> getAllDeclarations(
            @ParameterObject @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "dateCreated",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<DeclarationResponseDTO> page = declarationService.getDeclarationsForAgent(pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_AGENT') or @securityUtils.isDeclarationOwner(#id)")
    public ResponseEntity<DeclarationResponseDTO> updateDeclaration(
            @PathVariable Long id,
            @RequestBody DeclarationRequestDTO dto) throws ResourceNotFoundException {
        return new ResponseEntity<>(declarationService.updateDeclaration(id, dto), HttpStatus.OK);
    }

    /*@DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeclaration(@PathVariable Long id) throws ResourceNotFoundException {
        declarationService.deleteDeclaration(id);
        return ResponseEntity.noContent().build();
    }*/

    @PatchMapping("/archive/{id}")
    @PreAuthorize("hasRole('ROLE_AGENT') or @securityUtils.isDeclarationOwner(#id)")
    public ResponseEntity<DeclarationResponseDTO> archiveDeclaration(@PathVariable Long id) throws ResourceNotFoundException {
        DeclarationResponseDTO response = declarationService.archiveDeclaration(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<Page<DeclarationResponseDTO>> getDeclarations(
            @ParameterObject @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "dateCreated",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) DeclarationType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {

        Page<DeclarationResponseDTO> declarations = declarationService.getDeclarationsWithGeneralFilters(
                categoryId,
                campusId,
                locationId,
                status,
                type,
                startDate,
                pageable
        );

        return new ResponseEntity<>(declarations, HttpStatus.OK);
    }
}
