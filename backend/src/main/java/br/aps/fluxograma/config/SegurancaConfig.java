package br.aps.fluxograma.config;

import java.nio.charset.StandardCharsets;
import java.time.Clock;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import br.aps.fluxograma.security.PropriedadesJwt;
import br.aps.fluxograma.security.RespostasDeSeguranca;
import br.aps.fluxograma.security.TokenService;

/**
 * API sem estado: todos os endpoints, exceto login e cadastro, exigem o JWT. O papel vai na
 * claim "papel" e vira a autoridade ROLE_ALUNO, ROLE_COORDENADOR ou ROLE_ADMIN_INSTITUCIONAL.
 */
@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

	@Bean
	SecurityFilterChain filtroDeSeguranca(HttpSecurity http, RespostasDeSeguranca respostas,
			JwtAuthenticationConverter conversor) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.POST, "/auth/login", "/auth/registro")
				.permitAll()
				.requestMatchers("/swagger-ui", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs",
						"/v3/api-docs/**", "/error")
				.permitAll()
				.anyRequest()
				.authenticated())
			.oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(conversor))
				.authenticationEntryPoint(respostas)
				.accessDeniedHandler(respostas))
			.exceptionHandling(e -> e.authenticationEntryPoint(respostas).accessDeniedHandler(respostas));
		return http.build();
	}

	@Bean
	JwtAuthenticationConverter conversorDeJwt() {
		JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
		autoridades.setAuthoritiesClaimName(TokenService.CLAIM_PAPEL);
		autoridades.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
		conversor.setJwtGrantedAuthoritiesConverter(autoridades);
		return conversor;
	}

	@Bean
	SecretKey chaveJwt(PropriedadesJwt propriedades) {
		return new SecretKeySpec(propriedades.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey chave) {
		return NimbusJwtEncoder.withSecretKey(chave).algorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey chave) {
		return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	PasswordEncoder codificadorDeSenha() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	Clock relogio() {
		return Clock.systemDefaultZone();
	}
}
