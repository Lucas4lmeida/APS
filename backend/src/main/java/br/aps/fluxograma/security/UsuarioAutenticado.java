package br.aps.fluxograma.security;

import org.springframework.security.oauth2.jwt.Jwt;

import br.aps.fluxograma.erro.ApiException;
import br.aps.fluxograma.model.Papel;

/** Quem fez a requisição, lido do JWT. */
public record UsuarioAutenticado(Long id, Papel papel) {

	public static UsuarioAutenticado de(Jwt jwt) {
		if (jwt == null) {
			throw ApiException.naoAutenticado("Não autenticado: faça login para continuar");
		}
		return new UsuarioAutenticado(Long.valueOf(jwt.getSubject()),
				Papel.valueOf(jwt.getClaimAsString(TokenService.CLAIM_PAPEL)));
	}

	public boolean ehAluno() {
		return papel == Papel.ALUNO;
	}

	/** Aluno só acessa os próprios dados (403 para dados de outro aluno). */
	public void exigirProprioAluno(Long alunoId) {
		if (!ehAluno() || !id.equals(alunoId)) {
			throw ApiException.proibido("Acesso negado: o aluno só acessa os próprios dados");
		}
	}
}
