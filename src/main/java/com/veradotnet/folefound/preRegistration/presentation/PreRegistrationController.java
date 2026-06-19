package com.veradotnet.folefound.preRegistration.presentation;

import com.veradotnet.folefound.preRegistration.domain.service.PreRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/preRegistration")
public class PreRegistrationController {

    private final PreRegistrationService preRegistrationService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadCSVFile(@RequestParam("file") MultipartFile file) {

        preRegistrationService.importPreRegistrations(file);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully imported.");

        return ResponseEntity.ok(response);
    }
}
