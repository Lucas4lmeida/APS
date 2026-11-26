package br.aps.fluxograma.erro;

import java.util.List;

import org.springframework.http.HttpStatus;

/** Erro de regra de negócio com o código HTTP e a mensagem que vão para o cliente. */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	private final List<DetalheErro> detalhes;

	public ApiException(HttpStatus status, String mensagem, List<DetalheErro> detalhes) {
		super(mensagem);
		this.status = status;
		this.detalhes = detalhes == null ? List.of() : List.copyOf(detalhes);
	}

	public ApiException(HttpStatus status, String mensagem) {
		this(status, mensagem, List.of());
	}

	public static ApiException naoEncontrado(String mensagem) {
		return new ApiException(HttpStatus.NOT_FOUND, mensagem);
	}

	public static ApiException conflito(String mensagem) {
		return new ApiException(HttpStatus.CONFLICT, mensagem);
	}

	public static ApiException conflito(String mensagem, List<DetalheErro> detalhes) {
		return new ApiException(HttpStatus.CONFLICT, mensagem, detalhes);
	}

	public static ApiException proibido(String mensagem) {
		return new ApiException(HttpStatus.FORBIDDEN, mensagem);
	}

	public static ApiException naoAutenticado(String mensagem) {
		return new ApiException(HttpStatus.UNAUTHORIZED, mensagem);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public List<DetalheErro> getDetalhes() {
		return detalhes;
	}
}
