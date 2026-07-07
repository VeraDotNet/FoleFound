package com.veradotnet.folefound.restitution.presentation;

import com.veradotnet.folefound.restitution.application.dto.RestitutionDTO;
import com.veradotnet.folefound.restitution.domain.service.RestitutionService;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import com.veradotnet.folefound.shared.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/restitution")
@RequiredArgsConstructor
public class RestitutionController {

    private final RestitutionService restitutionService;

    @PostMapping("/complete/{matchingId}")
    public ResponseEntity<RestitutionDTO> completeRestitution(
            @PathVariable Long matchingId,
            @RequestParam(required = false) String note) throws ResourceNotFoundException {

        Long agentId = SecurityUtils.getCurrentUserId();

        RestitutionDTO response = restitutionService.createRestitution(matchingId, agentId, note);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestitutionDTO> getRestitution(@PathVariable Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(restitutionService.getRestitution(id));
    }

    @GetMapping
    public ResponseEntity<Page<RestitutionDTO>> getRestitutions(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "dateCreated", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(restitutionService.getRestitutions(pageable));
    }

    @PatchMapping("/note/{id}")
    public ResponseEntity<RestitutionDTO> updateNote(
            @PathVariable Long id,
            @RequestBody String newNote) throws ResourceNotFoundException {
        return ResponseEntity.ok(restitutionService.updateRestitutionNote(id, newNote));
    }
}
