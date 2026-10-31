package com.example.floodmonitor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Flood Monitor REST API",
                version = "1.0.0",
                description = "Vollständige Dokumentation der REST-Schnittstelle für das Hochwasser-Überwachungssystem. Unterstützt Echtzeit-Simulationen, dynamische Schwellenwerte, Content Negotiation (JSON/XML) und zentrale Fehlerbehandlung."
        )
)
public class OpenApiConfig {
}