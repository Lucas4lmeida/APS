package br.aps.fluxograma.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.aps.fluxograma.dto.FluxoDtos.CursoResposta;
import br.aps.fluxograma.dto.FluxoDtos.DisciplinaResposta;
import br.aps.fluxograma.dto.FluxoDtos.FluxoDetalhe;
import br.aps.fluxograma.dto.FluxoDtos.SemestreResposta;
import br.aps.fluxograma.model.Curso;
import br.aps.fluxograma.model.Disciplina;
import br.aps.fluxograma.model.Fluxo;
import br.aps.fluxograma.model.SemestreFluxo;
import br.aps.fluxograma.model.TipoDisciplina;
import br.aps.fluxograma.model.regras.EstadoDisciplina;
import br.aps.fluxograma.model.regras.RotuloFluxo;

/** Monta as respostas da API a partir do fluxo carregado. Precisa rodar dentro da transação. */
@Component
public class MapeadorFluxo {

	public CursoResposta curso(Curso curso) {
		return new CursoResposta(curso.getId(), curso.getCodigo(), curso.getNome());
	}

	/** @param estados estado de cada disciplina para o aluno, ou {@code null} fora do fluxograma */
	public FluxoDetalhe detalhe(FluxoCarregado carregado, RotuloFluxo rotulo, Map<Long, EstadoDisciplina> estados) {
		Fluxo fluxo = carregado.fluxo();
		Map<Long, Set<Long>> preRequisitos = carregado.preRequisitos();
		Map<Long, List<Long>> libera = carregado.libera();

		List<SemestreResposta> semestres = new ArrayList<>();
		for (SemestreFluxo semestre : carregado.semestres()) {
			List<DisciplinaResposta> doSemestre = carregado.disciplinas()
				.stream()
				.filter(d -> d.getTipo() == TipoDisciplina.OBRIGATORIA && d.getSemestre() == semestre.getNumero())
				.map(d -> disciplina(d, preRequisitos, libera, estados))
				.toList();
			semestres.add(new SemestreResposta(semestre.getNumero(), semestre.getTitulo(), doSemestre));
		}
		List<DisciplinaResposta> optativas = doTipo(carregado, TipoDisciplina.OPTATIVA, preRequisitos, libera, estados);
		List<DisciplinaResposta> externas = doTipo(carregado, TipoDisciplina.EXTERNA, preRequisitos, libera, estados);

		int creditosObrigatorios = carregado.disciplinas()
			.stream()
			.filter(d -> d.getTipo() == TipoDisciplina.OBRIGATORIA)
			.mapToInt(Disciplina::getCreditos)
			.sum();
		return new FluxoDetalhe(fluxo.getId(), curso(fluxo.getCurso()), fluxo.getNome(), fluxo.getVigenciaInicio(),
				fluxo.getStatus(), rotulo, creditosObrigatorios, fluxo.getCreditosOptativosExigidos(),
				creditosObrigatorios + fluxo.getCreditosOptativosExigidos(), semestres, optativas, externas);
	}

	public DisciplinaResposta disciplina(Disciplina d, Map<Long, Set<Long>> preRequisitos,
			Map<Long, List<Long>> libera, Map<Long, EstadoDisciplina> estados) {
		return new DisciplinaResposta(d.getId(), d.getCodigo(), d.getNome(), d.getCreditos(), d.getCargaHoraria(),
				d.getTipo(), d.getSemestre(), d.getOrdem(), d.isPendente(),
				List.copyOf(preRequisitos.getOrDefault(d.getId(), Set.of())),
				List.copyOf(libera.getOrDefault(d.getId(), List.of())), estados == null ? null : estados.get(d.getId()));
	}

	private List<DisciplinaResposta> doTipo(FluxoCarregado carregado, TipoDisciplina tipo,
			Map<Long, Set<Long>> preRequisitos, Map<Long, List<Long>> libera, Map<Long, EstadoDisciplina> estados) {
		return carregado.disciplinas()
			.stream()
			.filter(d -> d.getTipo() == tipo)
			.map(d -> disciplina(d, preRequisitos, libera, estados))
			.toList();
	}
}
