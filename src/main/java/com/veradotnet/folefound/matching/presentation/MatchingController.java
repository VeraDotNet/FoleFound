package com.veradotnet.folefound.matching.presentation;

import com.veradotnet.folefound.matching.application.dto.MatchingDTO;
import com.veradotnet.folefound.matching.application.enums.MatchingStatus;
import com.veradotnet.folefound.matching.domain.service.MatchingService;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
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
@RequestMapping("api/v1/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @PatchMapping("/{id}/review")
    public ResponseEntity<MatchingDTO> reviewMatching(
            @PathVariable Long id,
            @RequestParam boolean isApproved) throws ResourceNotFoundException {

        MatchingDTO updatedMatching = matchingService.processAgentReview(id, isApproved);
        return ResponseEntity.ok(updatedMatching);
    }

    @GetMapping
    public ResponseEntity<Page<MatchingDTO>> getMatchings(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "dateCreated", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) MatchingStatus status // 'required = false' rend le paramètre optionnel
           ) {

        Page<MatchingDTO> matchings = matchingService.getMatchings(status, pageable);
        return new ResponseEntity<>(matchings, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchingDTO> getMatchingById(@PathVariable Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(matchingService.getMatchingById(id));
    }
}
