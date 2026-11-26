package br.aps.fluxograma.model.regras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CadeiaPreRequisitosTest {

	// Probabilidade e Estatística → Avaliação de Desempenho → Redes de Computadores → Segurança em Redes
	private static final long PROBABILIDADE = 130;

	private static final long AVALIACAO = 134;

	private static final long REDES = 139;

	private static final long SEGURANCA = 158;

	private static final long CALCULO_II = 122;

	private static final Map<Long, Set<Long>> GRAFO = Map.of(
			CALCULO_II, Set.of(),
			PROBABILIDADE, Set.of(CALCULO_II),
			AVALIACAO, Set.of(PROBABILIDADE),
			REDES, Set.of(AVALIACAO),
			SEGURANCA, Set.of(REDES));

	@Test
	@DisplayName("Antes: diretos no nível 1, indiretos a partir do 2")
	void antes() {
		assertThat(CadeiaPreRequisitos.antes(REDES, GRAFO)).containsExactly(entry(AVALIACAO, 1),
				entry(PROBABILIDADE, 2), entry(CALCULO_II, 3));
	}

	@Test
	@DisplayName("Depois: o que a disciplina libera, direta e indiretamente")
	void depois() {
		assertThat(CadeiaPreRequisitos.depois(PROBABILIDADE, GRAFO)).containsExactly(entry(AVALIACAO, 1),
				entry(REDES, 2), entry(SEGURANCA, 3));
		assertThat(CadeiaPreRequisitos.depois(SEGURANCA, GRAFO)).isEmpty();
	}

	@Test
	@DisplayName("Em caminhos de tamanhos diferentes vale o menor nível")
	void menorNivel() {
		Map<Long, Set<Long>> grafo = Map.of(1L, Set.of(), 2L, Set.of(1L), 3L, Set.of(1L, 2L));

		assertThat(CadeiaPreRequisitos.antes(3L, grafo)).containsOnly(entry(1L, 1), entry(2L, 1));
	}

	@Test
	@DisplayName("Ciclo no cadastro não trava o cálculo")
	void ciclo() {
		Map<Long, Set<Long>> grafo = Map.of(1L, Set.of(2L), 2L, Set.of(1L));

		assertThat(CadeiaPreRequisitos.antes(1L, grafo)).containsOnly(entry(2L, 1));
	}
}
