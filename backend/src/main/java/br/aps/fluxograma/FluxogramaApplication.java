package br.aps.fluxograma;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FluxogramaApplication {

	/** Propriedade do JDK com a pasta dos sockets locais (AF_UNIX) usados internamente pelo NIO. */
	static final String PASTA_SOCKETS_JDK = "jdk.net.unixdomain.tmpdir";

	public static void main(String[] args) {
		ajustarSocketsLocaisNoWindows();
		SpringApplication.run(FluxogramaApplication.class, args);
	}

	/**
	 * No Windows, o servidor web (NIO) abre um socket local na pasta temporária do sistema. Em algumas
	 * máquinas isso falha ("Unable to establish loopback connection") e a API não sobe. Quando ninguém
	 * definiu a pasta, usamos ~/.aps-fluxograma/sockets. Não muda nada em Linux, macOS ou no Docker.
	 */
	static void ajustarSocketsLocaisNoWindows() {
		boolean windows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");
		if (!windows || System.getProperty(PASTA_SOCKETS_JDK) != null) {
			return;
		}
		Path pasta = Path.of(System.getProperty("user.home"), ".aps-fluxograma", "sockets");
		try {
			Files.createDirectories(pasta);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Não foi possível criar " + pasta, e);
		}
		System.setProperty(PASTA_SOCKETS_JDK, pasta.toString());
	}
}
