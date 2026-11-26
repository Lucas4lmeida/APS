package br.aps.fluxograma.model;

public enum TipoDisciplina {
	/** Tem semestre e posição (ordem) na linha do semestre. */
	OBRIGATORIA,
	/** Sem semestre: fica no catálogo de optativas (semestre 99 no CSV). */
	OPTATIVA,
	/** Pré-requisito que não pertence ao fluxo; não aparece nas linhas nem conta no progresso. */
	EXTERNA
}
