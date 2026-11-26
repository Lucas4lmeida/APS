package br.aps.fluxograma.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.aps.fluxograma.dto.FluxoDtos.CursoResposta;
import br.aps.fluxograma.dto.FluxoDtos.FluxoResumo;
import br.aps.fluxograma.security.UsuarioAutenticado;
import br.aps.fluxograma.service.FluxoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/cursos")
@Tag(name = "Cursos e fluxos", description = "HU06")
public class CursoController {

	private final FluxoService fluxoService;

	public CursoController(FluxoService fluxoService) {
		this.fluxoService = fluxoService;
	}

	@GetMapping
	@Operation(summary = "Listar cursos")
	public List<CursoResposta> listar() {
		return fluxoService.listarCursos();
	}

	@GetMapping("/{id}/fluxos")
	@Operation(summary = "Listar fluxos do curso com o rótulo calculado (aluno não vê rascunhos)")
	public List<FluxoResumo> fluxos(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		return fluxoService.listarFluxos(id, UsuarioAutenticado.de(jwt));
	}
}
