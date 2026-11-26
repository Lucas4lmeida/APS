package br.aps.fluxograma.model.regras;

import java.util.Collection;

import br.aps.fluxograma.model.TipoDisciplina;

/**
 * Resumo do fluxograma do aluno (HU08, HU07, HU15).
 * <p>
 * Contagens consideram obrigatórias e optativas; pré-requisitos externos não contam no
 * progresso. Total exigido = créditos das obrigatórias + créditos optativos exigidos pelo
 * fluxo. Créditos concluídos = obrigatórias cursadas + optativas cursadas (limitadas ao
 * exigido). O percentual é arredondado para baixo, então só chega a 100 quando tudo foi
 * concluído.
 */
public record ResumoProgresso(
		int percentualConcluido,
		int cursadas,
		int cursando,
		int disponiveis,
		int bloqueadas,
		int obrigatoriasTotal,
		int obrigatoriasCursadas,
		int creditosConcluidos,
		int totalCreditosExigidos,
		int cargaHorariaConcluida,
		int creditosOptativosCursados,
		int creditosOptativosCursando,
		int creditosOptativosExigidos,
		int creditosOptativosFaltantes) {

	/** Dados de uma disciplina necessários para o resumo. */
	public record Item(TipoDisciplina tipo, int creditos, int cargaHoraria, EstadoDisciplina estado) {
	}

	public static ResumoProgresso calcular(Collection<Item> disciplinas, int creditosOptativosExigidos) {
		int cursadas = 0;
		int cursando = 0;
		int disponiveis = 0;
		int bloqueadas = 0;
		int obrigatoriasTotal = 0;
		int obrigatoriasCursadas = 0;
		int creditosObrigatorios = 0;
		int creditosObrigatoriosCursados = 0;
		int cargaHorariaConcluida = 0;
		int creditosOptativosCursados = 0;
		int creditosOptativosCursando = 0;

		for (Item item : disciplinas) {
			if (item.tipo() == TipoDisciplina.EXTERNA) {
				continue;
			}
			boolean obrigatoria = item.tipo() == TipoDisciplina.OBRIGATORIA;
			if (obrigatoria) {
				obrigatoriasTotal++;
				creditosObrigatorios += item.creditos();
			}
			switch (item.estado()) {
				case CURSADA -> {
					cursadas++;
					cargaHorariaConcluida += item.cargaHoraria();
					if (obrigatoria) {
						obrigatoriasCursadas++;
						creditosObrigatoriosCursados += item.creditos();
					}
					else {
						creditosOptativosCursados += item.creditos();
					}
				}
				case CURSANDO -> {
					cursando++;
					if (!obrigatoria) {
						creditosOptativosCursando += item.creditos();
					}
				}
				case DISPONIVEL -> disponiveis++;
				case BLOQUEADA -> bloqueadas++;
			}
		}

		int totalExigido = creditosObrigatorios + creditosOptativosExigidos;
		int concluidos = creditosObrigatoriosCursados + Math.min(creditosOptativosCursados, creditosOptativosExigidos);
		int percentual = totalExigido == 0 ? 0 : (int) Math.floor(100.0 * concluidos / totalExigido);
		int optativosFaltantes = Math.max(0, creditosOptativosExigidos - creditosOptativosCursados);

		return new ResumoProgresso(percentual, cursadas, cursando, disponiveis, bloqueadas, obrigatoriasTotal,
				obrigatoriasCursadas, concluidos, totalExigido, cargaHorariaConcluida, creditosOptativosCursados,
				creditosOptativosCursando, creditosOptativosExigidos, optativosFaltantes);
	}
}
