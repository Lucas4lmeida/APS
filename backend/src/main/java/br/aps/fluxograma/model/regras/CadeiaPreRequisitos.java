package br.aps.fluxograma.model.regras;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cadeia de pré-requisitos de uma disciplina (HU09): o que vem antes dela e o que depende
 * dela, direta ou indiretamente. O nível 1 é a relação direta; 2 em diante, indireta.
 */
public final class CadeiaPreRequisitos {

	private CadeiaPreRequisitos() {
	}

	/**
	 * @param preRequisitos para cada disciplina, os ids dos seus pré-requisitos diretos
	 * @return disciplinas que vêm antes, na ordem da busca, com o menor nível em que aparecem
	 */
	public static Map<Long, Integer> antes(long disciplinaId, Map<Long, Set<Long>> preRequisitos) {
		return percorrer(disciplinaId, preRequisitos);
	}

	/** Disciplinas que dependem desta (o que ela libera), com o menor nível em que aparecem. */
	public static Map<Long, Integer> depois(long disciplinaId, Map<Long, Set<Long>> preRequisitos) {
		return percorrer(disciplinaId, inverter(preRequisitos));
	}

	/** Para cada disciplina, as que a têm como pré-requisito direto. */
	public static Map<Long, Set<Long>> inverter(Map<Long, Set<Long>> preRequisitos) {
		Map<Long, Set<Long>> libera = new HashMap<>();
		preRequisitos.forEach((disciplina, requisitos) -> {
			for (Long requisito : requisitos) {
				libera.computeIfAbsent(requisito, chave -> new LinkedHashSet<>()).add(disciplina);
			}
		});
		return libera;
	}

	private static Map<Long, Integer> percorrer(long inicio, Map<Long, Set<Long>> arestas) {
		Map<Long, Integer> nivel = new LinkedHashMap<>();
		Deque<Long> fila = new ArrayDeque<>();
		fila.add(inicio);
		Map<Long, Integer> distancia = new HashMap<>();
		distancia.put(inicio, 0);
		while (!fila.isEmpty()) {
			Long atual = fila.poll();
			int proximo = distancia.get(atual) + 1;
			for (Long vizinho : arestas.getOrDefault(atual, Set.of())) {
				if (!distancia.containsKey(vizinho)) {
					distancia.put(vizinho, proximo);
					nivel.put(vizinho, proximo);
					fila.add(vizinho);
				}
			}
		}
		return nivel;
	}
}
