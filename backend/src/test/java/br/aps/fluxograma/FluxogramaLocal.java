package br.aps.fluxograma;

import org.springframework.boot.SpringApplication;

/**
 * Sobe a API com um PostgreSQL embutido, para rodar sem Docker e sem PostgreSQL instalado:
 *
 * <pre>./mvnw spring-boot:test-run</pre>
 *
 * Os dados ficam em target/pg-embutido (somem com {@code ./mvnw clean}).
 */
public final class FluxogramaLocal {

	private FluxogramaLocal() {
	}

	public static void main(String[] args) {
		if (System.getProperty("aps.pg-embutido.dados") == null) {
			System.setProperty("aps.pg-embutido.dados", "target/pg-embutido");
		}
		SpringApplication.from(FluxogramaApplication::main).with(PostgresEmbutidoConfig.class).run(args);
	}
}
