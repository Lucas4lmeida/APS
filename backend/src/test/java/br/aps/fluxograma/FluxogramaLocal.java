package br.aps.fluxograma;

import java.nio.file.Path;

import org.springframework.boot.SpringApplication;

/**
 * Sobe a API com um PostgreSQL embutido, para rodar sem Docker e sem PostgreSQL instalado:
 *
 * <pre>./mvnw spring-boot:test-run</pre>
 *
 * Os dados ficam em {@code ~/.aps-fluxograma/pg-dados} (apague a pasta para recomeçar do zero).
 * A pasta fica fora do projeto de propósito: no Windows, caminhos longos passam do limite de 260
 * caracteres e o PostgreSQL não consegue criar os arquivos internos. Para usar outra pasta:
 * {@code -Daps.pg-embutido.dados=...}.
 */
public final class FluxogramaLocal {

	private FluxogramaLocal() {
	}

	public static void main(String[] args) {
		if (System.getProperty("aps.pg-embutido.dados") == null) {
			Path dados = Path.of(System.getProperty("user.home"), ".aps-fluxograma", "pg-dados");
			System.setProperty("aps.pg-embutido.dados", dados.toString());
		}
		SpringApplication.from(FluxogramaApplication::main).with(PostgresEmbutidoConfig.class).run(args);
	}
}
