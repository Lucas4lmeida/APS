package br.aps.fluxograma.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.aps.fluxograma.erro.ApiException;
import br.aps.fluxograma.erro.DetalheErro;
import br.aps.fluxograma.model.Curso;
import br.aps.fluxograma.model.Disciplina;
import br.aps.fluxograma.model.Fluxo;
import br.aps.fluxograma.model.SemestreFluxo;
import br.aps.fluxograma.model.StatusFluxo;
import br.aps.fluxograma.model.TipoDisciplina;
import br.aps.fluxograma.model.regras.SemestreLetivo;
import br.aps.fluxograma.repository.DisciplinaRepository;
import br.aps.fluxograma.repository.FluxoRepository;
import br.aps.fluxograma.repository.SemestreFluxoRepository;
import br.aps.fluxograma.service.LeitorCsvFluxo.Linha;

/**
 * Cria um fluxo a partir do CSV já lido (HU03). No MVP é usada pelo seed de desenvolvimento;
 * os endpoints de importação do coordenador ficam para a próxima etapa.
 */
@Service
public class ImportacaoFluxoService {

	private final FluxoRepository fluxoRepository;

	private final SemestreFluxoRepository semestreRepository;

	private final DisciplinaRepository disciplinaRepository;

	public ImportacaoFluxoService(FluxoRepository fluxoRepository, SemestreFluxoRepository semestreRepository,
			DisciplinaRepository disciplinaRepository) {
		this.fluxoRepository = fluxoRepository;
		this.semestreRepository = semestreRepository;
		this.disciplinaRepository = disciplinaRepository;
	}

	@Transactional
	public Fluxo criarFluxo(Curso curso, String nome, String vigenciaInicio, int creditosOptativosExigidos,
			StatusFluxo status, LeitorCsvFluxo.Resultado csv) {
		if (!csv.valido()) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
					"Arquivo CSV inválido. Nenhuma disciplina foi cadastrada.", csv.erros());
		}
		if (!SemestreLetivo.valido(vigenciaInicio)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Vigência inválida (use AAAA.S)",
					List.of(DetalheErro.campo("vigenciaInicio", "Formato AAAA.S, ex.: 2023.1")));
		}

		Fluxo fluxo = fluxoRepository.save(new Fluxo(curso, nome, vigenciaInicio, status, creditosOptativosExigidos));

		int ultimoSemestre = csv.linhas()
			.stream()
			.filter(l -> !l.optativa())
			.mapToInt(Linha::semestre)
			.max()
			.orElse(0);
		List<SemestreFluxo> semestres = new ArrayList<>();
		for (int numero = 1; numero <= ultimoSemestre; numero++) {
			semestres.add(new SemestreFluxo(fluxo, numero, null));
		}
		// As disciplinas referenciam (fluxo_id, semestre): os semestres precisam existir antes
		semestreRepository.saveAllAndFlush(semestres);

		Map<String, Disciplina> porCodigo = new LinkedHashMap<>();
		Map<Integer, Integer> proximaOrdem = new HashMap<>();
		for (Linha linha : csv.linhas()) {
			int chave = linha.optativa() ? LeitorCsvFluxo.SEMESTRE_OPTATIVAS : linha.semestre();
			int ordem = proximaOrdem.merge(chave, 1, Integer::sum);
			Disciplina disciplina = new Disciplina(fluxo, linha.codigo(), linha.nome(), linha.creditos(),
					linha.cargaHoraria(), linha.optativa() ? TipoDisciplina.OPTATIVA : TipoDisciplina.OBRIGATORIA,
					linha.optativa() ? null : linha.semestre(), ordem, false);
			porCodigo.put(linha.codigo(), disciplinaRepository.save(disciplina));
		}

		// Pré-requisitos fora do arquivo viram disciplinas EXTERNAS (pendentes quando sem "EXT:")
		Set<String> externosDeclarados = new HashSet<>();
		Set<String> foraDoArquivo = new LinkedHashSet<>();
		for (Linha linha : csv.linhas()) {
			for (String requisito : linha.preRequisitos()) {
				String codigo = Linha.codigoDe(requisito);
				if (!porCodigo.containsKey(codigo)) {
					foraDoArquivo.add(codigo);
					if (Linha.externo(requisito)) {
						externosDeclarados.add(codigo);
					}
				}
			}
		}
		int ordemExterna = 0;
		for (String codigo : foraDoArquivo) {
			boolean pendente = !externosDeclarados.contains(codigo);
			String nomeExterno = pendente ? "Código não encontrado no CSV" : "Pré-requisito externo";
			Disciplina externa = new Disciplina(fluxo, codigo, nomeExterno, 0, 0, TipoDisciplina.EXTERNA, null,
					++ordemExterna, pendente);
			porCodigo.put(codigo, disciplinaRepository.save(externa));
		}

		for (Linha linha : csv.linhas()) {
			Disciplina disciplina = porCodigo.get(linha.codigo());
			for (String requisito : linha.preRequisitos()) {
				disciplina.getPreRequisitos().add(porCodigo.get(Linha.codigoDe(requisito)));
			}
		}
		disciplinaRepository.saveAll(porCodigo.values());
		return fluxo;
	}
}
