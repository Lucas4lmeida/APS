package br.aps.fluxograma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FluxogramaApplication {

	public static void main(String[] args) {
		SpringApplication.run(FluxogramaApplication.class, args);
	}
}
