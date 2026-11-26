package br.aps.fluxograma.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do JWT ({@code app.jwt.*}). O segredo assina os tokens com HS256 e precisa ter
 * pelo menos 32 bytes; em produção vem da variável de ambiente JWT_SEGREDO.
 */
@ConfigurationProperties("app.jwt")
public record PropriedadesJwt(String segredo, Duration validade, String emissor) {

	public PropriedadesJwt {
		if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException(
					"Defina app.jwt.segredo (variável JWT_SEGREDO) com pelo menos 32 caracteres");
		}
		validade = validade == null ? Duration.ofHours(8) : validade;
		emissor = emissor == null || emissor.isBlank() ? "aps-fluxograma" : emissor;
	}
}
