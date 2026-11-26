package br.aps.fluxograma.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.aps.fluxograma.dto.AlunoDtos.AlunoResumo;
import br.aps.fluxograma.dto.AlunoDtos.FluxogramaResposta;
import br.aps.fluxograma.dto.AlunoDtos.MarcacaoRequest;
import br.aps.fluxograma.dto.AlunoDtos.MarcacaoResposta;
import br.aps.fluxograma.dto.AlunoDtos.SelecionarFluxoRequest;
import br.aps.fluxograma.security.UsuarioAutenticado;
import br.aps.fluxograma.service.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Área do aluno: só o próprio aluno acessa os seus dados (403 para outro aluno). */
@RestController
@RequestMapping("/alunos/{id}")
@PreAuthorize("hasRole('ALUNO')")
@Tag(name = "Aluno", description = "HU06, HU07, HU08, HU18, HU19")
public class AlunoController {

	private final AlunoService alunoService;

	public AlunoController(AlunoService alunoService) {
		this.alunoService = alunoService;
	}

	@PutMapping("/fluxo")
	@Operation(summary = "Selecionar o fluxo inicial (pode ser alterado enquanto nenhuma disciplina foi marcada)")
	public AlunoResumo selecionarFluxo(@PathVariable Long id, @Valid @RequestBody SelecionarFluxoRequest requisicao,
			@AuthenticationPrincipal Jwt jwt) {
		UsuarioAutenticado.de(jwt).exigirProprioAluno(id);
		return alunoService.selecionarFluxo(id, requisicao.fluxoId());
	}

	@GetMapping("/fluxograma")
	@Operation(summary = "Fluxograma com o estado de cada disciplina, resumo e créditos optativos")
	public FluxogramaResposta fluxograma(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		UsuarioAutenticado.de(jwt).exigirProprioAluno(id);
		return alunoService.fluxograma(id);
	}

	@PutMapping("/disciplinas/{disciplinaId}")
	@Operation(summary = "Marcar como CURSADA ou CURSANDO (inclusive pré-requisito externo, como CURSADA)")
	public MarcacaoResposta marcar(@PathVariable Long id, @PathVariable Long disciplinaId,
			@Valid @RequestBody MarcacaoRequest requisicao, @AuthenticationPrincipal Jwt jwt) {
		UsuarioAutenticado.de(jwt).exigirProprioAluno(id);
		return alunoService.marcar(id, disciplinaId, requisicao.situacao());
	}

	@DeleteMapping("/disciplinas/{disciplinaId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Desmarcar (trancou, reprovou ou marcou por engano): volta a Disponível ou Bloqueada")
	public void desmarcar(@PathVariable Long id, @PathVariable Long disciplinaId, @AuthenticationPrincipal Jwt jwt) {
		UsuarioAutenticado.de(jwt).exigirProprioAluno(id);
		alunoService.desmarcar(id, disciplinaId);
	}
}
