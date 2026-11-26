package br.aps.fluxograma.model.regras;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.aps.fluxograma.model.StatusFluxo;

/**
 * Rótulo calculado de um fluxo (docs/arquitetura.md, seção 3.1), a partir do semestre
 * letivo atual do curso:
 * <ul>
 * <li><b>Prévia</b>: publicado, vigência ainda não começou;</li>
 * <li><b>Vigente</b>: publicado, o de vigência mais recente já iniciada;</li>
 * <li><b>Em acompanhamento</b>: os demais publicados;</li>
 * <li><b>Rascunho</b>: não publicado (só a coordenação vê).</li>
 * </ul>
 */
public enum RotuloFluxo {
	RASCUNHO,
	PREVIA,
	VIGENTE,
	EM_ACOMPANHAMENTO;

	/** Dados de um fluxo do curso necessários para o cálculo. */
	public record Entrada(long fluxoId, StatusFluxo status, SemestreLetivo vigenciaInicio) {
	}

	/** Calcula o rótulo de cada fluxo de um mesmo curso. */
	public static Map<Long, RotuloFluxo> calcular(Collection<Entrada> fluxosDoCurso, SemestreLetivo atual) {
		Optional<SemestreLetivo> vigenciaDoVigente = fluxosDoCurso.stream()
			.filter(f -> f.status() == StatusFluxo.PUBLICADO)
			.map(Entrada::vigenciaInicio)
			.filter(v -> v.compareTo(atual) <= 0)
			.max(SemestreLetivo::compareTo);

		Map<Long, RotuloFluxo> rotulos = new HashMap<>();
		for (Entrada f : fluxosDoCurso) {
			RotuloFluxo rotulo;
			if (f.status() != StatusFluxo.PUBLICADO) {
				rotulo = RASCUNHO;
			}
			else if (f.vigenciaInicio().compareTo(atual) > 0) {
				rotulo = PREVIA;
			}
			else if (vigenciaDoVigente.isPresent() && f.vigenciaInicio().equals(vigenciaDoVigente.get())) {
				rotulo = VIGENTE;
			}
			else {
				rotulo = EM_ACOMPANHAMENTO;
			}
			rotulos.put(f.fluxoId(), rotulo);
		}
		return rotulos;
	}
}
