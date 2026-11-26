package br.aps.fluxograma.erro;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Converte exceções no formato único de erro da API. */
@RestControllerAdvice
public class TratadorDeErros {

	private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ErroResposta> api(ApiException e) {
		return resposta(e.getStatus(), new ErroResposta(e.getMessage(), e.getDetalhes()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException e) {
		List<DetalheErro> detalhes = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(TratadorDeErros::detalhe)
			.toList();
		return resposta(HttpStatus.BAD_REQUEST, new ErroResposta("Dados inválidos", detalhes));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException e) {
		return resposta(HttpStatus.BAD_REQUEST, ErroResposta.de("Requisição malformada"));
	}

	@ExceptionHandler({ MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class })
	ResponseEntity<ErroResposta> parametroInvalido(Exception e) {
		return resposta(HttpStatus.BAD_REQUEST, ErroResposta.de("Parâmetro inválido na requisição"));
	}

	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<ErroResposta> naoAutenticado(AuthenticationException e) {
		return resposta(HttpStatus.UNAUTHORIZED, ErroResposta.de("Não autenticado: faça login para continuar"));
	}

	@ExceptionHandler({ AccessDeniedException.class, AuthorizationDeniedException.class })
	ResponseEntity<ErroResposta> acessoNegado(Exception e) {
		return resposta(HttpStatus.FORBIDDEN, ErroResposta.de("Acesso negado"));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<ErroResposta> naoEncontrado(NoResourceFoundException e) {
		return resposta(HttpStatus.NOT_FOUND, ErroResposta.de("Recurso não encontrado"));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<ErroResposta> metodo(HttpRequestMethodNotSupportedException e) {
		return resposta(HttpStatus.METHOD_NOT_ALLOWED, ErroResposta.de("Método não permitido neste endpoint"));
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	ResponseEntity<ErroResposta> tipoDeMidia(HttpMediaTypeNotSupportedException e) {
		return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErroResposta.de("Envie o corpo como application/json"));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException e) {
		log.warn("Violação de integridade: {}", e.getMostSpecificCause().getMessage());
		return resposta(HttpStatus.CONFLICT, ErroResposta.de("Conflito com dados já cadastrados"));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErroResposta> inesperado(Exception e) {
		log.error("Erro inesperado", e);
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, ErroResposta.de("Erro interno do servidor"));
	}

	private static DetalheErro detalhe(FieldError erro) {
		return DetalheErro.campo(erro.getField(), erro.getDefaultMessage());
	}

	private static ResponseEntity<ErroResposta> resposta(HttpStatus status, ErroResposta corpo) {
		return ResponseEntity.status(status).body(corpo);
	}
}
