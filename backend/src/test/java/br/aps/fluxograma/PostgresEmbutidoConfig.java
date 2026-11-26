package br.aps.fluxograma;

import java.io.IOException;
import java.nio.file.Path;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.zaxxer.hikari.HikariDataSource;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * PostgreSQL embutido (binários do io.zonky), sem Docker e sem PostgreSQL instalado. Usado
 * nos testes de integração e em {@code ./mvnw spring-boot:test-run}.
 * <p>
 * Propriedades de sistema opcionais: {@code aps.pg-embutido.dados} (diretório para manter os
 * dados entre execuções) e {@code aps.pg-embutido.porta}.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresEmbutidoConfig {

	@Bean(destroyMethod = "close")
	EmbeddedPostgres postgresEmbutido() throws IOException {
		EmbeddedPostgres.Builder construtor = EmbeddedPostgres.builder();
		String dados = System.getProperty("aps.pg-embutido.dados");
		if (dados != null && !dados.isBlank()) {
			construtor.setDataDirectory(Path.of(dados).toAbsolutePath()).setCleanDataDirectory(false);
		}
		String porta = System.getProperty("aps.pg-embutido.porta");
		if (porta != null && !porta.isBlank()) {
			construtor.setPort(Integer.parseInt(porta));
		}
		return construtor.start();
	}

	@Bean
	DataSource dataSource(EmbeddedPostgres postgres) {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(postgres.getJdbcUrl("postgres", "postgres"));
		dataSource.setUsername("postgres");
		dataSource.setMaximumPoolSize(5);
		return dataSource;
	}
}
