package tn.esprit.gestionfoyer.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@OpenAPIDefinition(info = @Info(title = "GestionFoyer API", version = "1.0"))
public class FoyerConfiguration {
}
