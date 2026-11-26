package br.aps.fluxograma.erro;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato único de erro da API (docs/arquitetura.md, seção 4.6):
 * <pre>{ "erro": "...", "detalhes": [ { "linha": 26, "campo": "pre_requisitos" } ] }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErroResposta(String erro, List<DetalheErro> detalhes) {

	public ErroResposta {
		detalhes = detalhes == null ? List.of() : List.copyOf(detalhes);
	}

	public static ErroResposta de(String erro) {
		return new ErroResposta(erro, List.of());
	}
}
