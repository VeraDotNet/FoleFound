package com.veradotnet.folefound.declaration.application.scheduler;

import com.veradotnet.folefound.declaration.application.enums.DeclarationStatus;
import com.veradotnet.folefound.declaration.domain.model.Declaration;
import com.veradotnet.folefound.declaration.domain.repository.DeclarationRepo;
import com.veradotnet.folefound.item.application.enums.ItemState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArchiveScheduler {

    private final DeclarationRepo declarationRepo;

    // Se lance chaque 1er du mois
    @Scheduled(cron = "@monthly")
    @Transactional
    public void archiveOldDeclarations() {
        log.info("Starting automated archiving of declarations...");

        // Calcul de la date limite : aujourd'hui moins 30 jours
        LocalDateTime limitDate = LocalDateTime.now().minusDays(30);

        // Trouver toutes les déclarations "périmées"
        List<Declaration> oldDeclarations = declarationRepo.findAllByStatusAndLastModifiedBefore(
                DeclarationStatus.ACTIVE,
                limitDate
        );

        if (oldDeclarations.isEmpty()) {
            log.info("No obsolete declarations to archive.");
            return;
        }

        // Archivage en masse
        for (Declaration dec : oldDeclarations) {
            dec.setStatus(DeclarationStatus.ARCHIVED);
            if (dec.getItem() != null) {
                dec.getItem().setItemState(ItemState.ARCHIVED);
            }
        }

        declarationRepo.saveAll(oldDeclarations);
        log.info("{} declarations have been automatically archived.", oldDeclarations.size());
    }
}
