package br.aps.fluxograma.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.aps.fluxograma.dto.FluxoDtos.CursoResposta;
import br.aps.fluxograma.dto.FluxoDtos.DisciplinaDetalhe;
import br.aps.fluxograma.dto.FluxoDtos.FluxoDetalhe;
import br.aps.fluxograma.dto.FluxoDtos.FluxoResumo;
import br.aps.fluxograma.dto.FluxoDtos.ItemCadeia;
import br.aps.fluxograma.erro.ApiException;
import br.aps.fluxograma.model.Aluno;
import br.aps.fluxograma.model.Cursa;
import br.aps.fluxograma.model.Curso;
import br.aps.fluxograma.model.Disciplina;
import br.aps.fluxograma.model.Fluxo;
import br.aps.fluxograma.model.Situacao;
import br.aps.fluxograma.model.TipoDisciplina;
import br.aps.fluxograma.model.regras.CadeiaPreRequisitos;
import br.aps.fluxograma.model.regras.CalculoEstados;
import br.aps.fluxograma.model.regras.EstadoDisciplina;
import br.aps.fluxograma.model.regras.RotuloFluxo;
import br.aps.fluxograma.model.regras.SemestreLetivo;
import br.aps.fluxograma.repository.AlunoRepository;
import br.aps.fluxograma.repository.CoordenadorRepository;
import br.aps.fluxograma.repository.CursaRepository;
import br.aps.fluxograma.repository.CursoRepository;
import br.aps.fluxograma.repository.DisciplinaRepository;
import br.aps.fluxograma.repository.FluxoRepository;
import br.aps.fluxograma.repository.SemestreFluxoRepository;
import br.aps.fluxograma.security.UsuarioAutenticado;

/** Cursos, fluxos e disciplinas para consulta (HU06, HU08, HU09). */
@Service
public class FluxoService {

	private final CursoRepository cursoRepository;

	private final FluxoRepository fluxoRepository;

	private final SemestreFluxoRepository semestreRepository;

	private final DisciplinaRepository disciplinaRepository;

	private final CoordenadorRepository coordenadorRepository;

	private final AlunoRepository alunoRepository;

	private final CursaRepository cursaRepository;

	private final MapeadorFluxo mapeador;

	private final Clock relogio;

	public FluxoService(CursoRepository cursoRepository, FluxoRepository fluxoRepository,
			SemestreFluxoRepository semestreRepository, DisciplinaRepository disciplinaRepository,
			CoordenadorRepository coordenadorRepository, AlunoRepository alunoRepository,
			CursaRepository cursaRepository, MapeadorFluxo mapeador, Clock relogio) {
		this.cursoRepository = cursoRepository;
		this.fluxoRepository = fluxoRepository;
		this.semestreRepository = semestreRepository;
		this.disciplinaRepository = disciplinaRepository;
		this.coordenadorRepository = coordenadorRepository;
		this.alunoRepository = alunoRepository;
		this.cursaRepository = cursaRepository;
		this.mapeador = mapeador;
		this.relogio = relogio;
	}

	/**
	 * Semestre letivo atual do curso. Pela arquitetura é o do período de matrícula aberto ou,
	 * se não houver, o do último já iniciado; como períodos de matrícula ainda não fazem parte
	 * do MVP, usa a data de hoje (janeiro–junho = .1, julho–dezembro = .2).
	 */
	public SemestreLetivo semestreLetivoAtual(Long cursoId) {
		return SemestreLetivo.daData(LocalDate.now(relogio));
	}

	@Transactional(readOnly = true)
	public List<CursoResposta> listarCursos() {
		return cursoRepository.findAllByOrderByNomeAsc().stream().map(mapeador::curso).toList();
	}

	@Transactional(readOnly = true)
	public List<FluxoResumo> listarFluxos(Long cursoId, UsuarioAutenticado usuario) {
		Curso curso = cursoRepository.findById(cursoId)
			.orElseThrow(() -> ApiException.naoEncontrado("Curso não encontrado"));
		List<Fluxo> fluxos = fluxoRepository.findByCursoIdOrderByVigenciaInicioDesc(curso.getId());
		Map<Long, RotuloFluxo> rotulos = rotular(fluxos, curso.getId());
		boolean veRascunhos = podeVerRascunhos(usuario, curso.getId());

		Map<Long, Integer> semestres = new HashMap<>();
		for (Object[] linha : semestreRepository.contarPorFluxoDoCurso(curso.getId())) {
			semestres.put((Long) linha[0], ((Number) linha[1]).intValue());
		}
		Map<Long, Map<TipoDisciplina, int[]>> totais = new HashMap<>();
		for (Object[] linha : disciplinaRepository.totaisPorFluxoDoCurso(curso.getId())) {
			totais.computeIfAbsent((Long) linha[0], k -> new HashMap<>())
				.put((TipoDisciplina) linha[1],
						new int[] { ((Number) linha[2]).intValue(), ((Number) linha[3]).intValue() });
		}

		return fluxos.stream().filter(f -> veRascunhos || f.isPublicado()).map(f -> {
			Map<TipoDisciplina, int[]> doFluxo = totais.getOrDefault(f.getId(), Map.of());
			int[] obrigatorias = doFluxo.getOrDefault(TipoDisciplina.OBRIGATORIA, new int[2]);
			int[] optativas = doFluxo.getOrDefault(TipoDisciplina.OPTATIVA, new int[2]);
			return new FluxoResumo(f.getId(), curso.getId(), f.getNome(), f.getVigenciaInicio(), f.getStatus(),
					rotulos.get(f.getId()), semestres.getOrDefault(f.getId(), 0), obrigatorias[0], optativas[0],
					obrigatorias[1], f.getCreditosOptativosExigidos(),
					obrigatorias[1] + f.getCreditosOptativosExigidos());
		}).toList();
	}

	@Transactional(readOnly = true)
	public FluxoDetalhe detalhar(Long fluxoId, UsuarioAutenticado usuario) {
		FluxoCarregado carregado = carregar(fluxoId);
		exigirVisivel(carregado.fluxo(), usuario);
		return mapeador.detalhe(carregado, rotuloDe(carregado.fluxo()), null);
	}

	/** Detalhes da disciplina e a cadeia completa antes e depois (HU09). */
	@Transactional(readOnly = true)
	public DisciplinaDetalhe detalharDisciplina(Long disciplinaId, UsuarioAutenticado usuario) {
		Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
			.orElseThrow(() -> ApiException.naoEncontrado("Disciplina não encontrada"));
		Fluxo fluxo = disciplina.getFluxo();
		exigirVisivel(fluxo, usuario);
		FluxoCarregado carregado = carregar(fluxo.getId());
		Map<Long, Set<Long>> preRequisitos = carregado.preRequisitos();
		Map<Long, Disciplina> porId = carregado.porId();

		Map<Long, EstadoDisciplina> estados = estadosParaAluno(usuario, carregado);
		List<ItemCadeia> antes = itens(CadeiaPreRequisitos.antes(disciplinaId, preRequisitos), porId, estados);
		List<ItemCadeia> depois = itens(CadeiaPreRequisitos.depois(disciplinaId, preRequisitos), porId, estados);
		return new DisciplinaDetalhe(
				mapeador.disciplina(porId.get(disciplinaId), preRequisitos, carregado.libera(), estados),
				fluxo.getId(), fluxo.getNome(), antes, depois);
	}

	@Transactional(readOnly = true)
	public FluxoCarregado carregar(Long fluxoId) {
		Fluxo fluxo = fluxoRepository.findById(fluxoId)
			.orElseThrow(() -> ApiException.naoEncontrado("Fluxo não encontrado"));
		return new FluxoCarregado(fluxo, semestreRepository.findByIdFluxoIdOrderByIdNumeroAsc(fluxoId),
				disciplinaRepository.findByFluxoComPreRequisitos(fluxoId));
	}

	public RotuloFluxo rotuloDe(Fluxo fluxo) {
		Long cursoId = fluxo.getCurso().getId();
		return rotular(fluxoRepository.findByCursoIdOrderByVigenciaInicioDesc(cursoId), cursoId).get(fluxo.getId());
	}

	private Map<Long, RotuloFluxo> rotular(List<Fluxo> fluxosDoCurso, Long cursoId) {
		List<RotuloFluxo.Entrada> entradas = fluxosDoCurso.stream()
			.map(f -> new RotuloFluxo.Entrada(f.getId(), f.getStatus(), SemestreLetivo.parse(f.getVigenciaInicio())))
			.toList();
		return RotuloFluxo.calcular(entradas, semestreLetivoAtual(cursoId));
	}

	/** Rascunho só é visível para a coordenação do curso e para o admin institucional. */
	public void exigirVisivel(Fluxo fluxo, UsuarioAutenticado usuario) {
		if (!fluxo.isPublicado() && !podeVerRascunhos(usuario, fluxo.getCurso().getId())) {
			throw ApiException.naoEncontrado("Fluxo não encontrado");
		}
	}

	private boolean podeVerRascunhos(UsuarioAutenticado usuario, Long cursoId) {
		return switch (usuario.papel()) {
			case ADMIN_INSTITUCIONAL -> true;
			case COORDENADOR -> coordenadorRepository.findCursoIdByCoordenadorId(usuario.id())
				.map(cursoId::equals)
				.orElse(false);
			case ALUNO -> false;
		};
	}

	/** Estados só fazem sentido para o aluno que segue esse fluxo. */
	private Map<Long, EstadoDisciplina> estadosParaAluno(UsuarioAutenticado usuario, FluxoCarregado carregado) {
		if (!usuario.ehAluno()) {
			return null;
		}
		Aluno aluno = alunoRepository.findById(usuario.id()).orElse(null);
		if (aluno == null || aluno.getFluxo() == null || !aluno.getFluxo().getId().equals(carregado.fluxo().getId())) {
			return null;
		}
		Map<Long, Situacao> marcacoes = cursaRepository.findDoAlunoNoFluxo(aluno.getId(), carregado.fluxo().getId())
			.stream()
			.collect(Collectors.toMap(Cursa::getDisciplinaId, Cursa::getSituacao));
		return CalculoEstados.calcular(carregado.nos(), marcacoes);
	}

	private static List<ItemCadeia> itens(Map<Long, Integer> niveis, Map<Long, Disciplina> porId,
			Map<Long, EstadoDisciplina> estados) {
		return niveis.entrySet().stream().map(e -> {
			Disciplina d = porId.get(e.getKey());
			return new ItemCadeia(d.getId(), d.getCodigo(), d.getNome(), d.getTipo(), d.getSemestre(), e.getValue(),
					estados == null ? null : estados.get(d.getId()));
		})
			.sorted((a, b) -> FluxoCarregado.ORDEM_NO_FLUXO.compare(porId.get(a.id()), porId.get(b.id())))
			.toList();
	}
}
