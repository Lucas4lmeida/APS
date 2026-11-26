package br.aps.fluxograma.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import br.aps.fluxograma.dto.FluxoDtos.DisciplinaDetalhe;
import br.aps.fluxograma.dto.FluxoDtos.FluxoDetalhe;
import br.aps.fluxograma.security.UsuarioAutenticado;
import br.aps.fluxograma.service.FluxoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Cursos e fluxos", description = "HU08, HU09")
public class FluxoController {

	private final FluxoService fluxoService;

	public FluxoController(FluxoService fluxoService) {
		this.fluxoService = fluxoService;
	}

	@GetMapping("/fluxos/{id}")
	@Operation(summary = "Fluxo completo: semestres, disciplinas na ordem, pré-requisitos e o que cada uma libera")
	public FluxoDetalhe fluxo(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return fluxoService.detalhar(id, UsuarioAutenticado.de(jwt));
	}

	@GetMapping("/disciplinas/{id}")
	@Operation(summary = "Detalhes da disciplina e cadeia de pré-requisitos (antes e depois)")
	public DisciplinaDetalhe disciplina(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return fluxoService.detalharDisciplina(id, UsuarioAutenticado.de(jwt));
	}
}
