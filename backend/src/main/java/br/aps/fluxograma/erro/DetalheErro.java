package br.aps.fluxograma.erro;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Item de "detalhes" no formato único de erro: linha (no CSV), campo e mensagem. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetalheErro(Integer linha, String campo, String mensagem) {

	public static DetalheErro campo(String campo, String mensagem) {
		return new DetalheErro(null, campo, mensagem);
	}

	public static DetalheErro linha(int linha, String campo, String mensagem) {
		return new DetalheErro(linha, campo, mensagem);
	}
}
