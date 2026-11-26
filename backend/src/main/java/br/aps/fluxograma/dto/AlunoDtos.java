package br.aps.fluxograma.dto;

import br.aps.fluxograma.dto.FluxoDtos.FluxoDetalhe;
import br.aps.fluxograma.model.Situacao;
import br.aps.fluxograma.model.regras.ResumoProgresso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Área do aluno (HU06, HU07, HU08, HU18, HU19). */
public final class AlunoDtos {

	private AlunoDtos() {
	}

	public record AlunoResumo(Long id, String nome, String matricula, Long fluxoId) {
	}

	public record SelecionarFluxoRequest(@NotNull(message = "Informe o fluxo") Long fluxoId) {
	}

	public record MarcacaoRequest(
			@Schema(description = "CURSADA ou CURSANDO", example = "CURSANDO")
			@NotNull(message = "Informe a situação: CURSADA ou CURSANDO") Situacao situacao) {
	}

	public record MarcacaoResposta(Long disciplinaId, String codigo, Situacao situacao) {
	}

	/** Fluxo do aluno com o estado de cada disciplina e o resumo do progresso. */
	public record FluxogramaResposta(AlunoResumo aluno, FluxoDetalhe fluxo, ResumoProgresso resumo) {
	}
}
