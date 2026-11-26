package br.aps.fluxograma.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.aps.fluxograma.model.Disciplina;
import br.aps.fluxograma.model.Fluxo;
import br.aps.fluxograma.model.SemestreFluxo;
import br.aps.fluxograma.model.regras.CalculoEstados;

/** Fluxo com semestres e disciplinas (já com pré-requisitos) carregados de uma vez. */
public record FluxoCarregado(Fluxo fluxo, List<SemestreFluxo> semestres, List<Disciplina> disciplinas) {

	/** Obrigatórias por semestre e ordem; depois optativas; depois externas. */
	public static final Comparator<Disciplina> ORDEM_NO_FLUXO = Comparator
		.comparing((Disciplina d) -> d.getTipo().ordinal())
		.thenComparing(Disciplina::getSemestre, Comparator.nullsLast(Comparator.naturalOrder()))
		.thenComparingInt(Disciplina::getOrdem)
		.thenComparing(Disciplina::getCodigo);

	public FluxoCarregado {
		disciplinas = disciplinas.stream().sorted(ORDEM_NO_FLUXO).toList();
	}

	public Map<Long, Disciplina> porId() {
		return disciplinas.stream()
			.collect(Collectors.toMap(Disciplina::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
	}

	/** Para cada disciplina, os ids dos pré-requisitos diretos, na ordem do fluxo. */
	public Map<Long, Set<Long>> preRequisitos() {
		Map<Long, Set<Long>> grafo = new LinkedHashMap<>();
		for (Disciplina d : disciplinas) {
			Set<Long> requisitos = d.getPreRequisitos()
				.stream()
				.sorted(ORDEM_NO_FLUXO)
				.map(Disciplina::getId)
				.collect(Collectors.toCollection(LinkedHashSet::new));
			grafo.put(d.getId(), requisitos);
		}
		return grafo;
	}

	/** Para cada disciplina, os ids das que ela libera diretamente, na ordem do fluxo. */
	public Map<Long, List<Long>> libera() {
		Map<Long, List<Long>> libera = new LinkedHashMap<>();
		for (Disciplina d : disciplinas) {
			libera.put(d.getId(), new ArrayList<>());
		}
		for (Disciplina d : disciplinas) {
			for (Disciplina requisito : d.getPreRequisitos()) {
				libera.computeIfAbsent(requisito.getId(), k -> new ArrayList<>()).add(d.getId());
			}
		}
		return libera;
	}

	public List<CalculoEstados.No> nos() {
		return preRequisitos().entrySet().stream().map(e -> new CalculoEstados.No(e.getKey(), e.getValue())).toList();
	}
}
