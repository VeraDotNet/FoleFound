package com.veradotnet.folefound.matching.domain.service;

import com.veradotnet.folefound.declaration.application.enums.DeclarationStatus;
import com.veradotnet.folefound.declaration.application.enums.DeclarationType;
import com.veradotnet.folefound.declaration.domain.model.Declaration;
import com.veradotnet.folefound.declaration.domain.repository.DeclarationRepo;
import com.veradotnet.folefound.matching.application.enums.MatchingStatus;
import com.veradotnet.folefound.matching.domain.model.Matching;
import com.veradotnet.folefound.matching.domain.repository.MatchingRepo;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MatchingService {

    private final DeclarationRepo declarationRepo;
    private final MatchingRepo matchingRepo;
    private final EmailService emailService;

    @Transactional
    public void triggerMatching(Declaration newDeclaration) {

        // 1. Déterminer le type opposé à rechercher (Si LOST -> cherche TROUVE, et inversement)
        DeclarationType targetType = (newDeclaration.getType() == DeclarationType.LOST)
                ? DeclarationType.FOUND : DeclarationType.LOST;

        boolean isPerte = (newDeclaration.getType() == DeclarationType.LOST);

        // 2. Récupérer toutes les déclarations opposées qui sont encore actives et de la même categorie
        List<Declaration> potentials = declarationRepo.findByTypeAndStatusAndItemCategoryId(
                targetType,
                DeclarationStatus.ACTIVE,
                newDeclaration.getItem().getCategory().getId());

        for (Declaration potential : potentials) {

            // 3. Calcul probabiliste du score basé sur les attributs
            double score = calculateScore(newDeclaration, potential);

            // 4. Si la ressemblance est significative (ex: >= 50%), on crée une proposition de match
            if (score >= 50.0) {

                Matching matching = new Matching();
                // Assignation stricte des rôles Perte vs Trouvé pour la table de liaison
                matching.setLostDeclaration(isPerte ? newDeclaration : potential);
                matching.setFoundDeclaration(isPerte ? potential : newDeclaration);

                // 5. Application de tes règles de seuil (80%) pour définir le statut
                if (score >= 80.0) {
                    matching.setStatus(MatchingStatus.AUTOMATIC_VALIDATED);
                    matchingRepo.save(matching);
                    // Récupération de l'email de celui qui a perdu l'objet
                    String emailPerdeur = matching.getLostDeclaration().getUser().getEmail();
                    String objectName = matching.getLostDeclaration().getItem().getName();

                    emailService.sendSimpleEmail(
                            emailPerdeur,
                            "Objet trouvé - Une correspondance a été détectée !",
                            "Bonjour,\n\nBonne nouvelle ! Un objet correspondant à votre déclaration (" + objectName + ") a été ramené au guichet du campus.\n\nConnectez-vous sur votre espace FoleFound pour confirmer s'il s'agit bien du vôtre."
                    );
                } else if (score >= 50.0) {
                    // Si le score est moyen, on attend la validation de l'agent
                    matching.setStatus(MatchingStatus.PENDING_AGENT_REVIEW);
                    matchingRepo.save(matching);
                }
                // En dessous de 50%, on ignore, le match n'est pas assez pertinent
            }
        }
    }

    @Transactional
    public MatchingStatusDTO processAgentReview(Long matchingId, boolean isApproved) throws ResourceNotFoundException {
        Matching matching = matchingRepo.findById(matchingId)
                .orElseThrow(() -> new ResourceNotFoundException("Matching non trouvé"));

        if (matching.getStatus() != MatchingStatus.PENDING_AGENT_REVIEW) {
            throw new IllegalStateException("Ce matching n'est pas en attente de revue par un agent.");
        }

        if (isApproved) {
            // L'agent confirme que les objets correspondent !
            matching.setStatus(MatchingStatus.MANUAL_VALIDATED);
            // Ici, tu déclenches l'envoi de l'e-mail automatique à l'étudiant pour lui dire de venir au guichet
        } else {
            // L'agent dit que ça ne correspond pas
            matching.setStatus(MatchingStatus.REJECTED);
            // Les déclarations restent ACTIVE pour pouvoir matcher avec d'autres objets plus tard
        }

        matchingRepo.save(matching);
        return MatchingMapper.INSTANCE.toDTO(matching);
    }


    /**
     * Calcule le score de similarité classique (sur 100 points)
     */
    public double calculateScore(Declaration d1, Declaration d2) {
        // Règle éliminatoire stricte : Catégories différentes = Aucun match possible (0%)
        if (!d1.getItem().getCategory().getId().equals(d2.getItem().getCategory().getId())) {
            return 0.0;
        }

        double score = 40.0; // Poids de base car même catégorie d'objet

        // 🎨 Critère Couleur (30 points)
        if (d1.getItem().getColor().equalsIgnoreCase(d2.getItem().getColor())) {
            score += 30.0;
        } else if (d1.getItem().getColor().contains(d2.getItem().getColor()) ||
                d2.getItem().getColor().contains(d1.getItem().getColor())) {
            score += 15.0; // Couleur approchante (ex: "bleu" et "bleu marine")
        }

        // 📍 Critère Lieu / Campus (20 points)
        if (d1.getLocation().getId().equals(d2.getLocation().getId())) {
            score += 20.0;
        }

        // 📅 Critère Chronologique (10 points max si l'écart est faible)
        long daysBetween = Math.abs(ChronoUnit.DAYS.between(d1.getDateEvent(), d2.getDateEvent()));
        if (daysBetween <= 3) {
            score += 10.0;
        } else if (daysBetween <= 7) {
            score += 5.0;
        }

        return score;
    }
}
