package com.veradotnet.folefound.image.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class S3Service {
    // Ces valeurs viendront de ton application.properties
    private final String bucketName = "ton-bucket-folefound";

    public String uploadFile(MultipartFile file) {
        // 1. Générer un nom unique pour éviter d'écraser deux images nommées "photo.jpg"
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        try {
            /* CODE SDK AWS S3 FINAL (Exemple logique) :

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            */

            // 2. Retourner l'URL d'accès au fichier
            return "https://" + bucketName + ".s3.amazonaws.com/" + uniqueFileName;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du fichier vers S3", e);
        }
    }
}
