package br.aps.fluxograma.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.aps.fluxograma.dto.AuthDtos.LoginRequest;
import br.aps.fluxograma.dto.AuthDtos.RegistroRequest;
import br.aps.fluxograma.dto.AuthDtos.RespostaAutenticacao;
import br.aps.fluxograma.dto.AuthDtos.UsuarioResumo;
import br.aps.fluxograma.erro.ApiException;
import br.aps.fluxograma.erro.DetalheErro;
import br.aps.fluxograma.model.Aluno;
import br.aps.fluxograma.model.Coordenador;
import br.aps.fluxograma.model.Usuario;
import br.aps.fluxograma.repository.AlunoRepository;
import br.aps.fluxograma.repository.UsuarioRepository;
import br.aps.fluxograma.security.TokenService;
import br.aps.fluxograma.security.TokenService.TokenEmitido;

/** Login e cadastro do aluno (HU22). */
@Service
public class AuthService {

	/** Mesma mensagem para login e senha errados: não indica qual dos dois está incorreto. */
	static final String CREDENCIAIS_INVALIDAS = "E-mail/matrícula ou senha inválidos";

	private final UsuarioRepository usuarioRepository;

	private final AlunoRepository alunoRepository;

	private final PasswordEncoder codificador;

	private final TokenService tokenService;

	public AuthService(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository,
			PasswordEncoder codificador, TokenService tokenService) {
		this.usuarioRepository = usuarioRepository;
		this.alunoRepository = alunoRepository;
		this.codificador = codificador;
		this.tokenService = tokenService;
	}

	/** E-mail (qualquer papel) ou matrícula (aluno) + senha. */
	@Transactional(readOnly = true)
	public RespostaAutenticacao login(LoginRequest requisicao) {
		String login = requisicao.login().trim();
		Optional<Usuario> usuario = login.contains("@") ? usuarioRepository.findByEmailIgnoreCase(login)
				: alunoRepository.findByMatricula(login).map(Usuario.class::cast);
		if (usuario.isEmpty() || !codificador.matches(requisicao.senha(), usuario.get().getSenhaHash())) {
			throw ApiException.naoAutenticado(CREDENCIAIS_INVALIDAS);
		}
		return resposta(usuario.get());
	}

	/** Cria a conta do aluno (papel Aluno atribuído automaticamente) e já devolve o token. */
	@Transactional
	public RespostaAutenticacao registrar(RegistroRequest requisicao) {
		String email = requisicao.email().trim().toLowerCase(Locale.ROOT);
		String matricula = requisicao.matricula().trim();

		List<DetalheErro> emUso = new ArrayList<>();
		if (alunoRepository.existsByMatricula(matricula)) {
			emUso.add(DetalheErro.campo("matricula", "Matrícula já está em uso"));
		}
		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			emUso.add(DetalheErro.campo("email", "E-mail já está em uso"));
		}
		if (!emUso.isEmpty()) {
			String mensagem = emUso.size() == 1 ? emUso.get(0).mensagem() : "Matrícula e e-mail já estão em uso";
			throw ApiException.conflito(mensagem, emUso);
		}

		Aluno aluno = alunoRepository.save(
				new Aluno(email, requisicao.nome().trim(), codificador.encode(requisicao.senha()), matricula));
		return resposta(aluno);
	}

	private RespostaAutenticacao resposta(Usuario usuario) {
		TokenEmitido token = tokenService.emitir(usuario);
		return new RespostaAutenticacao(token.valor(), "Bearer", token.expiraEm(), resumo(usuario));
	}

	static UsuarioResumo resumo(Usuario usuario) {
		String matricula = null;
		Long fluxoId = null;
		Long cursoId = null;
		if (usuario instanceof Aluno aluno) {
			matricula = aluno.getMatricula();
			fluxoId = aluno.getFluxo() == null ? null : aluno.getFluxo().getId();
		}
		else if (usuario instanceof Coordenador coordenador) {
			cursoId = coordenador.getCurso().getId();
		}
		return new UsuarioResumo(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel(), matricula,
				fluxoId, cursoId);
	}
}
