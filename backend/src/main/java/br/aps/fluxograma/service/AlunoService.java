package br.aps.fluxograma.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.aps.fluxograma.dto.AlunoDtos.AlunoResumo;
import br.aps.fluxograma.dto.AlunoDtos.FluxogramaResposta;
import br.aps.fluxograma.dto.AlunoDtos.MarcacaoResposta;
import br.aps.fluxograma.dto.FluxoDtos.FluxoDetalhe;
import br.aps.fluxograma.erro.ApiException;
import br.aps.fluxograma.erro.DetalheErro;
import br.aps.fluxograma.model.Aluno;
import br.aps.fluxograma.model.Cursa;
import br.aps.fluxograma.model.CursaId;
import br.aps.fluxograma.model.Disciplina;
import br.aps.fluxograma.model.Fluxo;
import br.aps.fluxograma.model.Situacao;
import br.aps.fluxograma.model.TipoDisciplina;
import br.aps.fluxograma.model.regras.CalculoEstados;
import br.aps.fluxograma.model.regras.EstadoDisciplina;
import br.aps.fluxograma.model.regras.ResumoProgresso;
import br.aps.fluxograma.model.regras.RotuloFluxo;
import br.aps.fluxograma.repository.AlunoRepository;
import br.aps.fluxograma.repository.CursaRepository;
import br.aps.fluxograma.repository.DisciplinaRepository;
import br.aps.fluxograma.repository.FluxoRepository;

/** Fluxo do aluno, fluxograma com estados e marcações (HU06, HU07, HU08, HU18, HU19). */
@Service
public class AlunoService {

	private final AlunoRepository alunoRepository;

	private final FluxoRepository fluxoRepository;

	private final DisciplinaRepository disciplinaRepository;

	private final CursaRepository cursaRepository;

	private final FluxoService fluxoService;

	private final MapeadorFluxo mapeador;

	public AlunoService(AlunoRepository alunoRepository, FluxoRepository fluxoRepository,
			DisciplinaRepository disciplinaRepository, CursaRepository cursaRepository, FluxoService fluxoService,
			MapeadorFluxo mapeador) {
		this.alunoRepository = alunoRepository;
		this.fluxoRepository = fluxoRepository;
		this.disciplinaRepository = disciplinaRepository;
		this.cursaRepository = cursaRepository;
		this.fluxoService = fluxoService;
		this.mapeador = mapeador;
	}

	/**
	 * Seleção do fluxo inicial (HU06). Só fluxos publicados Vigentes ou Em acompanhamento.
	 * Enquanto o aluno não marcou nenhuma disciplina (cadastro inicial não confirmado), ele
	 * pode voltar e escolher outro; depois disso a troca é pela simulação (HU12/HU13).
	 */
	@Transactional
	public AlunoResumo selecionarFluxo(Long alunoId, Long fluxoId) {
		Aluno aluno = aluno(alunoId);
		Fluxo fluxo = fluxoRepository.findById(fluxoId)
			.filter(Fluxo::isPublicado)
			.orElseThrow(() -> ApiException.naoEncontrado("Fluxo não encontrado"));
		if (fluxoService.rotuloDe(fluxo) == RotuloFluxo.PREVIA) {
			throw ApiException.conflito(
					"O fluxo " + fluxo.getNome() + " ainda está em prévia: ele só aparece na simulação de troca de fluxo");
		}
		Fluxo atual = aluno.getFluxo();
		if (atual != null && !atual.getId().equals(fluxo.getId())
				&& cursaRepository.existeMarcacaoNoFluxo(aluno.getId(), atual.getId())) {
			throw ApiException.conflito("Você já marcou disciplinas no fluxo " + atual.getNome()
					+ ". A troca de fluxo é feita pela simulação de troca (HU12) e confirmada depois (HU13).");
		}
		aluno.setFluxo(fluxo);
		return resumo(aluno);
	}

	@Transactional(readOnly = true)
	public FluxogramaResposta fluxograma(Long alunoId) {
		Aluno aluno = aluno(alunoId);
		Fluxo fluxo = fluxoDo(aluno, "Nenhum fluxo selecionado. Escolha o seu fluxo antes de ver o fluxograma.");
		FluxoCarregado carregado = fluxoService.carregar(fluxo.getId());
		Map<Long, Situacao> marcacoes = marcacoes(aluno.getId(), fluxo.getId());
		Map<Long, EstadoDisciplina> estados = CalculoEstados.calcular(carregado.nos(), marcacoes);

		FluxoDetalhe detalhe = mapeador.detalhe(carregado, fluxoService.rotuloDe(fluxo), estados);
		List<ResumoProgresso.Item> itens = carregado.disciplinas()
			.stream()
			.map(d -> new ResumoProgresso.Item(d.getTipo(), d.getCreditos(), d.getCargaHoraria(), estados.get(d.getId())))
			.toList();
		ResumoProgresso resumo = ResumoProgresso.calcular(itens, fluxo.getCreditosOptativosExigidos());
		return new FluxogramaResposta(resumo(aluno), detalhe, resumo);
	}

	/**
	 * Marca a disciplina como CURSADA ou CURSANDO (HU07, HU18, HU09 para pré-requisito
	 * externo). Idempotente. Cursando exige todos os pré-requisitos cursados; pré-requisito
	 * externo só pode ser marcado como cumprido (CURSADA).
	 */
	@Transactional
	public MarcacaoResposta marcar(Long alunoId, Long disciplinaId, Situacao situacao) {
		Aluno aluno = aluno(alunoId);
		Disciplina disciplina = disciplinaDoFluxo(aluno, disciplinaId);

		if (situacao == Situacao.CURSANDO) {
			if (disciplina.getTipo() == TipoDisciplina.EXTERNA) {
				throw ApiException.conflito(
						"Pré-requisito externo só pode ser marcado como cumprido (CURSADA)");
			}
			Set<Long> requisitos = disciplina.getPreRequisitos()
				.stream()
				.map(Disciplina::getId)
				.collect(Collectors.toSet());
			Map<Long, Situacao> marcacoes = marcacoes(aluno.getId(), disciplina.getFluxo().getId());
			List<Long> pendentes = CalculoEstados
				.preRequisitosPendentes(new CalculoEstados.No(disciplina.getId(), requisitos), marcacoes);
			if (!pendentes.isEmpty()) {
				List<Disciplina> faltando = disciplina.getPreRequisitos()
					.stream()
					.filter(d -> pendentes.contains(d.getId()))
					.sorted(FluxoCarregado.ORDEM_NO_FLUXO)
					.toList();
				String codigos = faltando.stream().map(Disciplina::getCodigo).collect(Collectors.joining(", "));
				List<DetalheErro> detalhes = faltando.stream()
					.map(d -> DetalheErro.campo("preRequisitos", d.getCodigo() + " — " + d.getNome()))
					.toList();
				throw ApiException.conflito(
						"Para marcar como cursando, os pré-requisitos precisam estar cursados: " + codigos, detalhes);
			}
		}

		CursaId id = new CursaId(aluno.getId(), disciplina.getId());
		Cursa cursa = cursaRepository.findById(id).orElseGet(() -> new Cursa(aluno.getId(), disciplina.getId(), situacao));
		cursa.setSituacao(situacao);
		cursaRepository.save(cursa);
		return new MarcacaoResposta(disciplina.getId(), disciplina.getCodigo(), situacao);
	}

	/** Desmarca (trancou, reprovou ou marcou por engano — HU19). Idempotente. */
	@Transactional
	public void desmarcar(Long alunoId, Long disciplinaId) {
		Aluno aluno = aluno(alunoId);
		Disciplina disciplina = disciplinaDoFluxo(aluno, disciplinaId);
		CursaId id = new CursaId(aluno.getId(), disciplina.getId());
		cursaRepository.findById(id).ifPresent(cursaRepository::delete);
	}

	private Aluno aluno(Long alunoId) {
		return alunoRepository.findById(alunoId).orElseThrow(() -> ApiException.naoEncontrado("Aluno não encontrado"));
	}

	private Fluxo fluxoDo(Aluno aluno, String mensagemSemFluxo) {
		if (aluno.getFluxo() == null) {
			throw ApiException.naoEncontrado(mensagemSemFluxo);
		}
		return aluno.getFluxo();
	}

	private Disciplina disciplinaDoFluxo(Aluno aluno, Long disciplinaId) {
		if (aluno.getFluxo() == null) {
			throw ApiException.conflito("Selecione o seu fluxo antes de marcar disciplinas");
		}
		Long fluxoId = aluno.getFluxo().getId();
		return disciplinaRepository.findById(disciplinaId)
			.filter(d -> d.getFluxo().getId().equals(fluxoId))
			.orElseThrow(() -> ApiException.naoEncontrado("Disciplina não encontrada no seu fluxo"));
	}

	private Map<Long, Situacao> marcacoes(Long alunoId, Long fluxoId) {
		return cursaRepository.findDoAlunoNoFluxo(alunoId, fluxoId)
			.stream()
			.collect(Collectors.toMap(Cursa::getDisciplinaId, Cursa::getSituacao));
	}

	private static AlunoResumo resumo(Aluno aluno) {
		return new AlunoResumo(aluno.getId(), aluno.getNome(), aluno.getMatricula(),
				aluno.getFluxo() == null ? null : aluno.getFluxo().getId());
	}
}
