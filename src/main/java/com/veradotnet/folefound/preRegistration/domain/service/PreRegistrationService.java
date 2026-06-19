package com.veradotnet.folefound.preRegistration.domain.service;

import com.veradotnet.folefound.preRegistration.application.AcademicStatus;
import com.veradotnet.folefound.preRegistration.domain.model.PreRegistration;
import com.veradotnet.folefound.preRegistration.domain.repository.PreRegistrationRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PreRegistrationService {
    private final PreRegistrationRepo preRegistrationRepo;

    @Transactional
    public void importPreRegistrations(MultipartFile file) {
        // Vérification élémentaire du fichier
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide.");
        }

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT
                             .withFirstRecordAsHeader() // Utilise la 1ère ligne comme en-têtes
                             .withIgnoreHeaderCase()    // Ignore la casse
                             .withTrim())) {            // Nettoie les espaces vides autour des textes

            List<PreRegistration> preRegistrationsToSave = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord record : csvRecords) {

                // On ne récupère que la colonne "matricule"
                String matricule = record.get("matricule");

                if (matricule != null && !matricule.isBlank()) {
                    // On vérifie si ce matricule n'est pas déjà dans la liste blanche pour éviter les doublons
                    if (!preRegistrationRepo.existsByStudentCodeAndAcademicStatus(matricule, AcademicStatus.Active)) {

                        PreRegistration preRegistration = PreRegistration.builder()
                                .studentCode(matricule)
                                .academicStatus(AcademicStatus.Active) // Statut par défaut à l'import
                                .build();

                        preRegistrationsToSave.add(preRegistration);
                    }
                }
            }

            // Sauvegarde optimisée par lots (Batch) pour de meilleures performances
            if (!preRegistrationsToSave.isEmpty()) {
                preRegistrationRepo.saveAll(preRegistrationsToSave);
            }

        } catch (IOException e) {
            throw new RuntimeException(" CSV file treatment error: " + e.getMessage());
        }
    }
}
