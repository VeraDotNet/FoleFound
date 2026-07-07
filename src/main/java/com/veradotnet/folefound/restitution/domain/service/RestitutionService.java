package com.veradotnet.folefound.restitution.domain.service;

import com.veradotnet.folefound.declaration.application.enums.DeclarationStatus;
import com.veradotnet.folefound.item.application.enums.ItemState;
import com.veradotnet.folefound.matching.application.enums.MatchingStatus;
import com.veradotnet.folefound.matching.domain.model.Matching;
import com.veradotnet.folefound.matching.domain.repository.MatchingRepo;
import com.veradotnet.folefound.restitution.application.dto.RestitutionDTO;
import com.veradotnet.folefound.restitution.application.mapper.RestitutionMapper;
import com.veradotnet.folefound.restitution.domain.model.Restitution;
import com.veradotnet.folefound.restitution.domain.repository.RestitutionRepo;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import com.veradotnet.folefound.users.domain.model.Users;
import com.veradotnet.folefound.users.domain.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestitutionService {

    private final RestitutionRepo restitutionRepo;
    private final MatchingRepo matchingRepo;
    private final UserRepo usersRepo; // Pour récupérer l'agent

    @Transactional
    public RestitutionDTO createRestitution(Long matchingId, Long agentId, String note) throws ResourceNotFoundException {
        // 1. Récupérer le matching
        Matching matching = matchingRepo.findById(matchingId)
                .orElseThrow(() -> new RuntimeException("Matching not found"));

        if (matching.getStatus() == MatchingStatus.PENDING_AGENT_REVIEW){
            throw new IllegalStateException("This matching is not yet approved. It needs agent review");
        }
        if (matching.getStatus() == MatchingStatus.RESOLVED) {
            throw new IllegalStateException("This matching is already completed.");
        }

        // 2. Récupérer l'agent qui fait la manipulation
        Users agent = usersRepo.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        // 3. Mettre à jour les statuts de clôture
        matching.setStatus(MatchingStatus.RESOLVED);
        matching.getLostDeclaration().setStatus(DeclarationStatus.RESOLVED);
        matching.getFoundDeclaration().setStatus(DeclarationStatus.RESOLVED);

        //mise à jour du statut de l'item
        if (matching.getFoundDeclaration().getItem() != null) {
            matching.getFoundDeclaration().getItem().setItemState(ItemState.RESTITUTED);
            matching.getLostDeclaration().getItem().setItemState(ItemState.RESTITUTED);
        }

        matchingRepo.save(matching);

        // 4. Créer la restitution
        Restitution restitution = new Restitution();
        restitution.setMatching(matching);
        restitution.setAgent(agent);
        restitution.setNote(note);

        restitutionRepo.save(restitution);

        // 5. Retourner le DTO au contrôleur pour le Frontend
        return RestitutionMapper.INSTANCE.toDTO(restitution);
    }

    public RestitutionDTO getRestitution(Long id) throws ResourceNotFoundException {
        Restitution restitution = restitutionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restitution not found"));

        return RestitutionMapper.INSTANCE.toDTO(restitution);
    }

    public Page<RestitutionDTO> getRestitutions(Pageable pageable) {
        Page<Restitution> restitutions = restitutionRepo.findAll(pageable);

        return restitutions
                .map(RestitutionMapper.INSTANCE::toDTO);
    }

    @Transactional
    public RestitutionDTO updateRestitutionNote(Long id, String newNote) throws ResourceNotFoundException {
        Restitution restitution = restitutionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restitution not found"));

        // On ne met à jour QUE la note pour garder l'intégrité de l'historique
        if (newNote != null) {
            restitution.setNote(newNote.trim());
        }

        Restitution updated = restitutionRepo.save(restitution);
        return RestitutionMapper.INSTANCE.toDTO(updated);
    }
}
