package br.aps.fluxograma.model.regras;

import br.aps.fluxograma.model.Situacao;

/** Estado de uma disciplina para um aluno (HU08). */
public enum EstadoDisciplina {
	CURSADA,
	CURSANDO,
	DISPONIVEL,
	BLOQUEADA;

	public static EstadoDisciplina de(Situacao situacao) {
		return situacao == Situacao.CURSADA ? CURSADA : CURSANDO;
	}
}
