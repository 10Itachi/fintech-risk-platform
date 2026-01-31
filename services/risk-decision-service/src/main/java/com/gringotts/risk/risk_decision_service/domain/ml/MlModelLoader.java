package com.gringotts.risk.risk_decision_service.domain.ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Getter
@Component
public class MlModelLoader {

    private final MlModelArtifact model;

    public MlModelLoader(ObjectMapper mapper) {
        try (InputStream is =
                     new ClassPathResource("ml/logistic_model.json").getInputStream()) {
            String content = new String(is.readAllBytes());
            System.out.println("DEBUG RAW JSON: " + content);
            InputStream is2 = new ClassPathResource("ml/logistic_model.json").getInputStream();
            this.model = mapper.readValue(is2, MlModelArtifact.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load ML model", e);
        }
    }

}
