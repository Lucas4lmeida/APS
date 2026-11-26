package br.aps.fluxograma.model.regras;

import static br.aps.fluxograma.model.TipoDisciplina.EXTERNA;
import static br.aps.fluxograma.model.TipoDisciplina.OBRIGATORIA;
import static br.aps.fluxograma.model.TipoDisciplina.OPTATIVA;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.aps.fluxograma.model.regras.ResumoProgresso.Item;

class ResumoProgressoTest {

	@Test
	@DisplayName("Contagens, créditos e percentual; externas não contam")
	void resumo() {
		List<Item> itens = List.of(
				new Item(OBRIGATORIA, 4, 68, EstadoDisciplina.CURSADA),
				new Item(OBRIGATORIA, 4, 68, EstadoDisciplina.CURSANDO),
				new Item(OBRIGATORIA, 4, 68, EstadoDisciplina.DISPONIVEL),
				new Item(OBRIGATORIA, 4, 68, EstadoDisciplina.BLOQUEADA),
				new Item(OPTATIVA, 4, 68, EstadoDisciplina.CURSADA),
				new Item(OPTATIVA, 12, 204, EstadoDisciplina.CURSANDO),
				new Item(EXTERNA, 0, 0, EstadoDisciplina.CURSADA));

		ResumoProgresso resumo = ResumoProgresso.calcular(itens, 8);

		assertThat(resumo.cursadas()).isEqualTo(2);
		assertThat(resumo.cursando()).isEqualTo(2);
		assertThat(resumo.disponiveis()).isEqualTo(1);
		assertThat(resumo.bloqueadas()).isEqualTo(1);
		assertThat(resumo.obrigatoriasTotal()).isEqualTo(4);
		assertThat(resumo.obrigatoriasCursadas()).isEqualTo(1);
		assertThat(resumo.cargaHorariaConcluida()).isEqualTo(136);
		assertThat(resumo.creditosOptativosCursados()).isEqualTo(4);
		assertThat(resumo.creditosOptativosCursando()).isEqualTo(12);
		assertThat(resumo.creditosOptativosFaltantes()).isEqualTo(4);
		// total exigido = 16 (obrigatórias) + 8 (optativos); concluídos = 4 + 4
		assertThat(resumo.totalCreditosExigidos()).isEqualTo(24);
		assertThat(resumo.creditosConcluidos()).isEqualTo(8);
		assertThat(resumo.percentualConcluido()).isEqualTo(33);
	}

	@Test
	@DisplayName("Optativas além do exigido não passam de 100%")
	void optativasLimitadasAoExigido() {
		List<Item> itens = List.of(new Item(OBRIGATORIA, 4, 68, EstadoDisciplina.CURSADA),
				new Item(OPTATIVA, 4, 68, EstadoDisciplina.CURSADA), new Item(OPTATIVA, 4, 68, EstadoDisciplina.CURSADA));

		ResumoProgresso resumo = ResumoProgresso.calcular(itens, 4);

		assertThat(resumo.creditosConcluidos()).isEqualTo(8);
		assertThat(resumo.percentualConcluido()).isEqualTo(100);
		assertThat(resumo.creditosOptativosFaltantes()).isZero();
	}

	@Test
	@DisplayName("Exemplo do protótipo: 23 cursadas de 4 créditos = 92 de 194 (47%)")
	void exemploDoPrototipo() {
		List<Item> itens = new ArrayList<>();
		for (int i = 0; i < 44; i++) {
			// 43 × 4 cr + Estágio 6 cr = 178 cr
			itens.add(new Item(OBRIGATORIA, i == 43 ? 6 : 4, 68,
					i < 23 ? EstadoDisciplina.CURSADA : EstadoDisciplina.BLOQUEADA));
		}

		ResumoProgresso resumo = ResumoProgresso.calcular(itens, 16);

		assertThat(resumo.totalCreditosExigidos()).isEqualTo(194);
		assertThat(resumo.creditosConcluidos()).isEqualTo(92);
		assertThat(resumo.percentualConcluido()).isEqualTo(47);
	}

	@Test
	@DisplayName("Fluxo vazio não divide por zero")
	void vazio() {
		assertThat(ResumoProgresso.calcular(List.of(), 0).percentualConcluido()).isZero();
	}
}
