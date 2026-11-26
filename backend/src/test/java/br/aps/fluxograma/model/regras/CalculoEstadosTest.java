package br.aps.fluxograma.model.regras;

import static br.aps.fluxograma.model.regras.EstadoDisciplina.BLOQUEADA;
import static br.aps.fluxograma.model.regras.EstadoDisciplina.CURSADA;
import static br.aps.fluxograma.model.regras.EstadoDisciplina.CURSANDO;
import static br.aps.fluxograma.model.regras.EstadoDisciplina.DISPONIVEL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.aps.fluxograma.model.Situacao;
import br.aps.fluxograma.model.regras.CalculoEstados.No;

class CalculoEstadosTest {

	// Trecho do fluxo 2023.1: CC117 Cálculo I → CC122 Cálculo II; CC116 Discreta → CC125 Lógica
	private static final long CALCULO_I = 117;

	private static final long CALCULO_II = 122;

	private static final long DISCRETA = 116;

	private static final long LOGICA = 125;

	private static final long COMBINADA = 900; // exige Cálculo II e Lógica

	private static final long EXTERNA_CT904 = 904;

	private static final long PADROES = 983; // exige EXT:CT904

	private static final List<No> FLUXO = List.of(
			new No(CALCULO_I, Set.of()),
			new No(CALCULO_II, Set.of(CALCULO_I)),
			new No(DISCRETA, Set.of()),
			new No(LOGICA, Set.of(DISCRETA)),
			new No(COMBINADA, Set.of(CALCULO_II, LOGICA)),
			new No(EXTERNA_CT904, Set.of()),
			new No(PADROES, Set.of(EXTERNA_CT904)));

	@Test
	@DisplayName("Sem marcações: só quem não tem pré-requisito fica Disponível")
	void semMarcacoes() {
		Map<Long, EstadoDisciplina> estados = CalculoEstados.calcular(FLUXO, Map.of());

		assertThat(estados).containsEntry(CALCULO_I, DISPONIVEL)
			.containsEntry(DISCRETA, DISPONIVEL)
			.containsEntry(CALCULO_II, BLOQUEADA)
			.containsEntry(LOGICA, BLOQUEADA)
			.containsEntry(COMBINADA, BLOQUEADA)
			.containsEntry(PADROES, BLOQUEADA);
	}

	@Test
	@DisplayName("Marcar o pré-requisito como cursada libera a disciplina seguinte")
	void cursadaLibera() {
		Map<Long, EstadoDisciplina> estados = CalculoEstados.calcular(FLUXO, Map.of(CALCULO_I, Situacao.CURSADA));

		assertThat(estados).containsEntry(CALCULO_I, CURSADA).containsEntry(CALCULO_II, DISPONIVEL);
	}

	@Test
	@DisplayName("Pré-requisito múltiplo é relação E: um só não basta")
	void preRequisitoMultiploExigeTodos() {
		Map<Long, Situacao> marcacoes = new HashMap<>(Map.of(CALCULO_I, Situacao.CURSADA, CALCULO_II,
				Situacao.CURSADA, DISCRETA, Situacao.CURSADA));
		assertThat(CalculoEstados.calcular(FLUXO, marcacoes)).containsEntry(COMBINADA, BLOQUEADA);

		marcacoes.put(LOGICA, Situacao.CURSADA);
		assertThat(CalculoEstados.calcular(FLUXO, marcacoes)).containsEntry(COMBINADA, DISPONIVEL);
	}

	@Test
	@DisplayName("Pré-requisito em Cursando ainda não libera")
	void cursandoNaoLibera() {
		Map<Long, EstadoDisciplina> estados = CalculoEstados.calcular(FLUXO, Map.of(CALCULO_I, Situacao.CURSANDO));

		assertThat(estados).containsEntry(CALCULO_I, CURSANDO).containsEntry(CALCULO_II, BLOQUEADA);
	}

	@Test
	@DisplayName("A marcação do aluno prevalece sobre o cálculo")
	void marcacaoPrevalece() {
		// O aluno informa o próprio histórico: uma disciplina pode estar cursada mesmo com pré-requisito desmarcado
		Map<Long, EstadoDisciplina> estados = CalculoEstados.calcular(FLUXO, Map.of(CALCULO_II, Situacao.CURSADA));

		assertThat(estados).containsEntry(CALCULO_II, CURSADA).containsEntry(CALCULO_I, DISPONIVEL);
	}

	@Test
	@DisplayName("Desmarcar uma cursada volta a bloquear o que dependia dela (HU19)")
	void desmarcarRecalcula() {
		Map<Long, Situacao> marcacoes = new HashMap<>(Map.of(CALCULO_I, Situacao.CURSADA));
		assertThat(CalculoEstados.calcular(FLUXO, marcacoes)).containsEntry(CALCULO_II, DISPONIVEL);

		marcacoes.remove(CALCULO_I);
		assertThat(CalculoEstados.calcular(FLUXO, marcacoes)).containsEntry(CALCULO_I, DISPONIVEL)
			.containsEntry(CALCULO_II, BLOQUEADA);
	}

	@Test
	@DisplayName("Pré-requisito externo cumprido libera a disciplina (HU09)")
	void preRequisitoExterno() {
		assertThat(CalculoEstados.calcular(FLUXO, Map.of())).containsEntry(PADROES, BLOQUEADA);
		assertThat(CalculoEstados.calcular(FLUXO, Map.of(EXTERNA_CT904, Situacao.CURSADA))).containsEntry(PADROES,
				DISPONIVEL);
	}

	@Test
	@DisplayName("Lista os pré-requisitos que faltam, na ordem cadastrada")
	void pendentes() {
		No combinada = FLUXO.get(4);

		assertThat(CalculoEstados.preRequisitosPendentes(combinada, Map.of(LOGICA, Situacao.CURSANDO)))
			.containsExactlyInAnyOrder(CALCULO_II, LOGICA);
		assertThat(CalculoEstados.preRequisitosPendentes(combinada, Map.of(LOGICA, Situacao.CURSADA)))
			.containsExactly(CALCULO_II);
	}
}
