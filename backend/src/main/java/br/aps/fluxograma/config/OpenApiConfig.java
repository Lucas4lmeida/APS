package br.aps.fluxograma.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/** Documentação da API em /swagger-ui (springdoc-openapi). */
@Configuration
@OpenAPIDefinition(
		info = @Info(title = "APS — Fluxograma Interativo para Planejamento Acadêmico", version = "0.1.0",
				description = "MVP da fatia do aluno. Faça login em /auth/login e use o token em Authorize."),
		security = @SecurityRequirement(name = "jwt"))
@SecurityScheme(name = "jwt", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
