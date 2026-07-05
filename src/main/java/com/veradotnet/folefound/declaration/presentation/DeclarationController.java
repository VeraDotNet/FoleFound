package com.veradotnet.folefound.declaration.presentation;

import com.veradotnet.folefound.declaration.application.dto.DeclarationRequestDTO;
import com.veradotnet.folefound.declaration.application.dto.DeclarationResponseDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<DeclarationResponseDTO> getDeclarationById(@PathVariable Long id) throws ResourceNotFoundException {
        return new ResponseEntity<>(declarationService.getDeclaration(id), HttpStatus.OK);
    }

    @GetMapping("/my-declarations")
    public ResponseEntity<Page<DeclarationResponseDTO>> getMyDeclarations(
            @ParameterObject @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        Page<DeclarationResponseDTO> page = declarationService.getDeclarationsByUserId(currentUserId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping()
    public ResponseEntity<Page<DeclarationResponseDTO>> getAllDeclarations(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<DeclarationResponseDTO> page = declarationService.getDeclarationsForAgent(pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeclarationResponseDTO> updateDeclaration(
            @PathVariable Long id,
            @RequestBody DeclarationRequestDTO dto) throws ResourceNotFoundException {
        return new ResponseEntity<>(declarationService.updateDeclaration(id, dto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeclaration(@PathVariable Long id) throws ResourceNotFoundException {
        declarationService.deleteDeclaration(id);
        return ResponseEntity.noContent().build();
    }
}
