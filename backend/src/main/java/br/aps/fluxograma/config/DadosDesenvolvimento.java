package br.aps.fluxograma.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import br.aps.fluxograma.model.AdminInstitucional;
import br.aps.fluxograma.model.Aluno;
import br.aps.fluxograma.model.Coordenador;
import br.aps.fluxograma.model.Curso;
import br.aps.fluxograma.model.Cursa;
import br.aps.fluxograma.model.Fluxo;
import br.aps.fluxograma.model.Situacao;
import br.aps.fluxograma.model.StatusFluxo;
import br.aps.fluxograma.model.TipoDisciplina;
import br.aps.fluxograma.repository.AlunoRepository;
import br.aps.fluxograma.repository.CursaRepository;
import br.aps.fluxograma.repository.CursoRepository;
import br.aps.fluxograma.repository.DisciplinaRepository;
import br.aps.fluxograma.repository.FluxoRepository;
import br.aps.fluxograma.repository.UsuarioRepository;
import br.aps.fluxograma.service.ImportacaoFluxoService;
import br.aps.fluxograma.service.LeitorCsvFluxo;

/**
 * Dados de desenvolvimento (perfil "dev", o padrão). Idempotente: só cria o que ainda não
 * existe. As senhas abaixo valem só para desenvolvimento e estão documentadas no README.
 * <ul>
 * <li>curso CC e fluxo 2023.1 publicado, lido de docs/exemplos/modelo-fluxo.csv;</li>
 * <li>admin institucional e coordenador de exemplo;</li>
 * <li>aluno de demonstração (nome genérico) com os semestres 1 a 4 cursados.</li>
 * </ul>
 */
@Component
@Profile("dev")
public class DadosDesenvolvimento implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DadosDesenvolvimento.class);

	public static final String CURSO_CODIGO = "CC";

	public static final String FLUXO_NOME = "2023.1";

	public static final int FLUXO_CREDITOS_OPTATIVOS = 16;

	public static final String CSV_FLUXO = "dados-dev/modelo-fluxo.csv";

	public static final String ADMIN_EMAIL = "admin@aps.dev";

	public static final String ADMIN_SENHA = "admin123";

	public static final String COORDENADOR_EMAIL = "coordenacao.cc@aps.dev";

	public static final String COORDENADOR_SENHA = "coord123";

	public static final String ALUNO_EMAIL = "aluno.demo@aps.dev";

	public static final String ALUNO_NOME = "Aluno Demonstração";

	public static final String ALUNO_MATRICULA = "2023100001";

	public static final String ALUNO_SENHA = "aluno123";

	/** O aluno de demonstração já concluiu os semestres 1 a 4 (23 disciplinas). */
	private static final int ALUNO_SEMESTRES_CURSADOS = 4;

	private final CursoRepository cursoRepository;

	private final FluxoRepository fluxoRepository;

	private final DisciplinaRepository disciplinaRepository;

	private final UsuarioRepository usuarioRepository;

	private final AlunoRepository alunoRepository;

	private final CursaRepository cursaRepository;

	private final ImportacaoFluxoService importacao;

	private final PasswordEncoder codificador;

	private final TransactionTemplate transacao;

	public DadosDesenvolvimento(CursoRepository cursoRepository, FluxoRepository fluxoRepository,
			DisciplinaRepository disciplinaRepository, UsuarioRepository usuarioRepository,
			AlunoRepository alunoRepository, CursaRepository cursaRepository, ImportacaoFluxoService importacao,
			PasswordEncoder codificador, TransactionTemplate transacao) {
		this.cursoRepository = cursoRepository;
		this.fluxoRepository = fluxoRepository;
		this.disciplinaRepository = disciplinaRepository;
		this.usuarioRepository = usuarioRepository;
		this.alunoRepository = alunoRepository;
		this.cursaRepository = cursaRepository;
		this.importacao = importacao;
		this.codificador = codificador;
		this.transacao = transacao;
	}

	@Override
	public void run(ApplicationArguments args) {
		transacao.executeWithoutResult(status -> carregar());
		log.info("Dados de desenvolvimento prontos. Logins: {} / {} (admin), {} / {} (coordenador), "
				+ "{} ou {} / {} (aluno de demonstração)", ADMIN_EMAIL, ADMIN_SENHA, COORDENADOR_EMAIL,
				COORDENADOR_SENHA, ALUNO_MATRICULA, ALUNO_EMAIL, ALUNO_SENHA);
	}

	private void carregar() {
		Curso curso = cursoRepository.findByCodigo(CURSO_CODIGO)
			.orElseGet(() -> cursoRepository.save(new Curso(CURSO_CODIGO, "Ciência da Computação")));

		Fluxo fluxo = fluxoRepository.findByCursoIdAndNome(curso.getId(), FLUXO_NOME)
			.orElseGet(() -> importarFluxo(curso));

		if (!usuarioRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)) {
			usuarioRepository.save(
					new AdminInstitucional(ADMIN_EMAIL, "Admin Institucional", codificador.encode(ADMIN_SENHA)));
		}
		if (!usuarioRepository.existsByEmailIgnoreCase(COORDENADOR_EMAIL)) {
			usuarioRepository.save(new Coordenador(COORDENADOR_EMAIL, "Coordenação de Ciência da Computação",
					codificador.encode(COORDENADOR_SENHA), curso));
		}
		if (!alunoRepository.existsByMatricula(ALUNO_MATRICULA)) {
			Aluno demo = new Aluno(ALUNO_EMAIL, ALUNO_NOME, codificador.encode(ALUNO_SENHA), ALUNO_MATRICULA);
			demo.setFluxo(fluxo);
			Aluno salvo = alunoRepository.save(demo);
			disciplinaRepository.findByFluxoComPreRequisitos(fluxo.getId())
				.stream()
				.filter(d -> d.getTipo() == TipoDisciplina.OBRIGATORIA && d.getSemestre() <= ALUNO_SEMESTRES_CURSADOS)
				.forEach(d -> cursaRepository.save(new Cursa(salvo.getId(), d.getId(), Situacao.CURSADA)));
		}
	}

	private Fluxo importarFluxo(Curso curso) {
		try (Reader leitor = new InputStreamReader(new ClassPathResource(CSV_FLUXO).getInputStream(),
				StandardCharsets.UTF_8)) {
			LeitorCsvFluxo.Resultado csv = LeitorCsvFluxo.ler(leitor);
			Fluxo fluxo = importacao.criarFluxo(curso, FLUXO_NOME, FLUXO_NOME, FLUXO_CREDITOS_OPTATIVOS,
					StatusFluxo.PUBLICADO, csv);
			log.info("Fluxo {} importado de {} ({} disciplinas, {} avisos)", FLUXO_NOME, CSV_FLUXO,
					csv.linhas().size(), csv.avisos().size());
			return fluxo;
		}
		catch (IOException e) {
			throw new IllegalStateException("Não foi possível ler " + CSV_FLUXO, e);
		}
	}
}
