package br.aps.fluxograma.model.regras;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.aps.fluxograma.model.StatusFluxo;
import br.aps.fluxograma.model.regras.RotuloFluxo.Entrada;

class RotuloFluxoTest {

	@Test
	@DisplayName("Prévia, Vigente, Em acompanhamento e Rascunho a partir do semestre atual")
	void rotulos() {
		List<Entrada> fluxos = List.of(
				new Entrada(1, StatusFluxo.PUBLICADO, SemestreLetivo.parse("2015.1")),
				new Entrada(2, StatusFluxo.PUBLICADO, SemestreLetivo.parse("2023.1")),
				new Entrada(3, StatusFluxo.PUBLICADO, SemestreLetivo.parse("2027.1")),
				new Entrada(4, StatusFluxo.RASCUNHO, SemestreLetivo.parse("2026.1")));

		Map<Long, RotuloFluxo> rotulos = RotuloFluxo.calcular(fluxos, SemestreLetivo.parse("2026.2"));

		assertThat(rotulos).containsEntry(1L, RotuloFluxo.EM_ACOMPANHAMENTO)
			.containsEntry(2L, RotuloFluxo.VIGENTE)
			.containsEntry(3L, RotuloFluxo.PREVIA)
			.containsEntry(4L, RotuloFluxo.RASCUNHO);
	}

	@Test
	@DisplayName("Fluxo cuja vigência começa no semestre atual já é o vigente")
	void vigenciaNoSemestreAtual() {
		List<Entrada> fluxos = List.of(new Entrada(1, StatusFluxo.PUBLICADO, SemestreLetivo.parse("2023.1")),
				new Entrada(2, StatusFluxo.PUBLICADO, SemestreLetivo.parse("2026.2")));

		assertThat(RotuloFluxo.calcular(fluxos, SemestreLetivo.parse("2026.2"))).containsEntry(1L,
				RotuloFluxo.EM_ACOMPANHAMENTO)
			.containsEntry(2L, RotuloFluxo.VIGENTE);
	}

	@Test
	@DisplayName("Semestre letivo: formato AAAA.S e semestre da data")
	void semestreLetivo() {
		assertThat(SemestreLetivo.daData(LocalDate.of(2026, 11, 25))).hasToString("2026.2");
		assertThat(SemestreLetivo.daData(LocalDate.of(2026, 6, 30))).hasToString("2026.1");
		assertThat(SemestreLetivo.parse("2023.1")).isLessThan(SemestreLetivo.parse("2023.2"));
		assertThat(SemestreLetivo.parse("2023.2")).isLessThan(SemestreLetivo.parse("2024.1"));
		assertThatThrownBy(() -> SemestreLetivo.parse("2023.3")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> SemestreLetivo.parse("23.1")).isInstanceOf(IllegalArgumentException.class);
	}
}
