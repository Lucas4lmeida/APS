package br.aps.fluxograma.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 401 e 403 gerados pelos filtros do Spring Security passam pelo mesmo tratador de erros da
 * API, para manter o formato único { "erro": ..., "detalhes": [...] }.
 */
@Component
public class RespostasDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final HandlerExceptionResolver resolvedor;

	public RespostasDeSeguranca(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolvedor) {
		this.resolvedor = resolvedor;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException excecao) {
		response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
		resolvedor.resolveException(request, response, null, excecao);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException excecao) {
		resolvedor.resolveException(request, response, null, excecao);
	}
}
