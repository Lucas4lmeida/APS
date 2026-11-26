package br.aps.fluxograma.model.regras;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.aps.fluxograma.model.Situacao;

/**
 * Regra de cálculo do estado (docs/arquitetura.md, seção 5).
 * <ul>
 * <li>Cursada e Cursando vêm das marcações do aluno;</li>
 * <li><b>Disponível</b>: não está cursada nem cursando e <b>todos</b> os pré-requisitos
 * (inclusive externos) estão CURSADA — relação "E";</li>
 * <li>caso contrário, <b>Bloqueada</b>. Um pré-requisito em Cursando ainda não libera.</li>
 * </ul>
 * Não depende de JPA nem de Spring, para ser testada isoladamente.
 */
public final class CalculoEstados {

	private CalculoEstados() {
	}

	/** Disciplina vista como nó do grafo de pré-requisitos. */
	public record No(long id, Set<Long> preRequisitos) {
		public No {
			preRequisitos = Collections.unmodifiableSet(new LinkedHashSet<>(preRequisitos));
		}
	}

	public static Map<Long, EstadoDisciplina> calcular(Collection<No> disciplinas, Map<Long, Situacao> marcacoes) {
		Map<Long, EstadoDisciplina> estados = new LinkedHashMap<>();
		for (No no : disciplinas) {
			estados.put(no.id(), estadoDe(no, marcacoes));
		}
		return estados;
	}

	public static EstadoDisciplina estadoDe(No disciplina, Map<Long, Situacao> marcacoes) {
		Situacao marcada = marcacoes.get(disciplina.id());
		if (marcada != null) {
			return EstadoDisciplina.de(marcada);
		}
		return preRequisitosPendentes(disciplina, marcacoes).isEmpty() ? EstadoDisciplina.DISPONIVEL
				: EstadoDisciplina.BLOQUEADA;
	}

	/** Pré-requisitos que ainda não estão CURSADA (em Cursando também conta como pendente). */
	public static List<Long> preRequisitosPendentes(No disciplina, Map<Long, Situacao> marcacoes) {
		List<Long> pendentes = new ArrayList<>();
		for (Long requisito : disciplina.preRequisitos()) {
			if (marcacoes.get(requisito) != Situacao.CURSADA) {
				pendentes.add(requisito);
			}
		}
		return pendentes;
	}
}
