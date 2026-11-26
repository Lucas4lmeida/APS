package br.aps.fluxograma.security;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.aps.fluxograma.model.Usuario;

/** Emite o JWT com o id do usuário em "sub" e o papel na claim "papel". */
@Service
public class TokenService {

	public static final String CLAIM_PAPEL = "papel";

	private final JwtEncoder encoder;

	private final PropriedadesJwt propriedades;

	private final Clock relogio;

	public TokenService(JwtEncoder encoder, PropriedadesJwt propriedades, Clock relogio) {
		this.encoder = encoder;
		this.propriedades = propriedades;
		this.relogio = relogio;
	}

	public record TokenEmitido(String valor, Instant expiraEm) {
	}

	public TokenEmitido emitir(Usuario usuario) {
		Instant agora = relogio.instant();
		Instant expiraEm = agora.plus(propriedades.validade());
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(propriedades.emissor())
			.subject(String.valueOf(usuario.getId()))
			.issuedAt(agora)
			.expiresAt(expiraEm)
			.claim(CLAIM_PAPEL, usuario.getPapel().name())
			.claim("nome", usuario.getNome())
			.build();
		JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
		String valor = encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
		return new TokenEmitido(valor, expiraEm);
	}
}
