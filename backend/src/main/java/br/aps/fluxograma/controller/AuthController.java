package br.aps.fluxograma.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.aps.fluxograma.dto.AuthDtos.LoginRequest;
import br.aps.fluxograma.dto.AuthDtos.RegistroRequest;
import br.aps.fluxograma.dto.AuthDtos.RespostaAutenticacao;
import br.aps.fluxograma.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "HU22")
@SecurityRequirements
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	@Operation(summary = "Login com e-mail ou matrícula + senha; devolve o JWT com o papel")
	public RespostaAutenticacao login(@Valid @RequestBody LoginRequest requisicao) {
		return authService.login(requisicao);
	}

	@PostMapping("/registro")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastro do aluno (matrícula, nome, e-mail, senha); já devolve o JWT")
	public RespostaAutenticacao registrar(@Valid @RequestBody RegistroRequest requisicao) {
		return authService.registrar(requisicao);
	}
}
