package br.aps.fluxograma.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.aps.fluxograma.model.StatusFluxo;
import br.aps.fluxograma.model.TipoDisciplina;
import br.aps.fluxograma.model.regras.EstadoDisciplina;
import br.aps.fluxograma.model.regras.RotuloFluxo;

/** Cursos, fluxos e disciplinas (HU06, HU08, HU09). */
public final class FluxoDtos {

	private FluxoDtos() {
	}

	public record CursoResposta(Long id, String codigo, String nome) {
	}

	/** Item da lista de fluxos do curso, com o rótulo calculado. */
	public record FluxoResumo(Long id, Long cursoId, String nome, String vigenciaInicio, StatusFluxo status,
			RotuloFluxo rotulo, int quantidadeSemestres, int quantidadeObrigatorias, int quantidadeOptativas,
			int creditosObrigatorios, int creditosOptativosExigidos, int totalCreditosExigidos) {
	}

	/**
	 * Disciplina do fluxo. {@code preRequisitos} e {@code libera} trazem ids de disciplinas do
	 * mesmo fluxo. {@code estado} só vem no fluxograma do aluno.
	 */
	public record DisciplinaResposta(Long id, String codigo, String nome, int creditos, int cargaHoraria,
			TipoDisciplina tipo, Integer semestre, int ordem, boolean pendente, List<Long> preRequisitos,
			List<Long> libera, @JsonInclude(JsonInclude.Include.NON_NULL) EstadoDisciplina estado) {
	}

	public record SemestreResposta(int numero, String titulo, List<DisciplinaResposta> disciplinas) {
	}

	/** Fluxo completo: semestres na ordem, disciplinas na ordem do editor, optativas e externas. */
	public record FluxoDetalhe(Long id, CursoResposta curso, String nome, String vigenciaInicio, StatusFluxo status,
			RotuloFluxo rotulo, int creditosObrigatorios, int creditosOptativosExigidos, int totalCreditosExigidos,
			List<SemestreResposta> semestres, List<DisciplinaResposta> optativas, List<DisciplinaResposta> externas) {
	}

	/** Disciplina na cadeia de pré-requisitos: nível 1 = direto; 2 ou mais = indireto. */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record ItemCadeia(Long id, String codigo, String nome, TipoDisciplina tipo, Integer semestre, int nivel,
			EstadoDisciplina estado) {
	}

	/** Detalhes da disciplina e a cadeia antes e depois dela (HU09). */
	public record DisciplinaDetalhe(DisciplinaResposta disciplina, Long fluxoId, String fluxoNome,
			List<ItemCadeia> cadeiaAntes, List<ItemCadeia> cadeiaDepois) {
	}
}
