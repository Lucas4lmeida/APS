package br.aps.fluxograma.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.aps.fluxograma.model.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Requisições e respostas de autenticação (HU22). */
public final class AuthDtos {

	private AuthDtos() {
	}

	public record LoginRequest(
			@Schema(description = "E-mail ou matrícula (matrícula só para aluno)", example = "2023100001")
			@NotBlank(message = "Informe o e-mail ou a matrícula") String login,
			@NotBlank(message = "Informe a senha") String senha) {
	}

	public record RegistroRequest(
			@Schema(example = "2024100123")
			@NotBlank(message = "Informe a matrícula")
			@Size(max = 20, message = "A matrícula tem no máximo 20 caracteres")
			@Pattern(regexp = "^[A-Za-z0-9.-]+$", message = "Use apenas letras, números, ponto ou hífen")
			String matricula,
			@NotBlank(message = "Informe o nome") @Size(max = 255) String nome,
			@NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") @Size(max = 255) String email,
			@NotBlank(message = "Informe a senha")
			@Size(min = 6, max = 100, message = "A senha precisa ter entre 6 e 100 caracteres") String senha) {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record UsuarioResumo(Long id, String nome, String email, Papel papel, String matricula, Long fluxoId,
			Long cursoId) {
	}

	public record RespostaAutenticacao(String token, String tipo, Instant expiraEm, UsuarioResumo usuario) {
	}
}
