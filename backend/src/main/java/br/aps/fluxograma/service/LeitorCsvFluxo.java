package br.aps.fluxograma.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import br.aps.fluxograma.erro.DetalheErro;

/**
 * Lê o CSV de fluxo no formato de docs/arquitetura.md, seção 3.3:
 *
 * <pre>codigo;nome;semestre;creditos;carga_horaria;pre_requisitos;optativa</pre>
 *
 * Separador ";", UTF-8, cabeçalho obrigatório, semestre 99 para optativas, pré-requisitos
 * separados por vírgula, pré-requisito externo com prefixo "EXT:" e optativa "Sim"/"Não".
 * <p>
 * Erros de formato impedem a importação (cada um com linha e campo). Pré-requisito que não
 * está no arquivo e não tem "EXT:" não é erro: vira aviso e, na importação, disciplina
 * externa pendente (HU03, HU20).
 */
public final class LeitorCsvFluxo {

	public static final List<String> CABECALHO = List.of("codigo", "nome", "semestre", "creditos", "carga_horaria",
			"pre_requisitos", "optativa");

	public static final int SEMESTRE_OPTATIVAS = 99;

	public static final String PREFIXO_EXTERNO = "EXT:";

	private static final Pattern CODIGO = Pattern.compile("^[A-Za-z0-9._-]{1,20}$");

	private LeitorCsvFluxo() {
	}

	/**
	 * Linha válida do arquivo. {@code preRequisitos} guarda os códigos como no arquivo
	 * (externos com o prefixo "EXT:").
	 */
	public record Linha(int numero, String codigo, String nome, int semestre, int creditos, int cargaHoraria,
			List<String> preRequisitos, boolean optativa) {

		public static boolean externo(String preRequisito) {
			return preRequisito.startsWith(PREFIXO_EXTERNO);
		}

		public static String codigoDe(String preRequisito) {
			return externo(preRequisito) ? preRequisito.substring(PREFIXO_EXTERNO.length()) : preRequisito;
		}
	}

	public record Resultado(List<Linha> linhas, List<DetalheErro> erros, List<DetalheErro> avisos) {

		public boolean valido() {
			return erros.isEmpty();
		}
	}

	public static Resultado ler(String conteudo) {
		try {
			return ler(new StringReader(conteudo));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static Resultado ler(Reader leitor) throws IOException {
		List<String> texto = new BufferedReader(leitor).lines().toList();
		List<DetalheErro> erros = new ArrayList<>();
		List<DetalheErro> avisos = new ArrayList<>();
		List<Linha> linhas = new ArrayList<>();

		if (texto.isEmpty() || texto.get(0).isBlank()) {
			erros.add(DetalheErro.linha(1, "cabecalho", "Arquivo vazio ou sem cabeçalho"));
			return new Resultado(linhas, erros, avisos);
		}
		String primeira = texto.get(0).replace("﻿", "");
		List<String> cabecalho = Arrays.stream(primeira.split(";", -1))
			.map(c -> c.trim().toLowerCase(Locale.ROOT))
			.toList();
		if (!cabecalho.equals(CABECALHO)) {
			erros.add(DetalheErro.linha(1, "cabecalho", "Cabeçalho esperado: " + String.join(";", CABECALHO)));
			return new Resultado(linhas, erros, avisos);
		}

		Map<String, Integer> linhaDoCodigo = new HashMap<>();
		for (int i = 1; i < texto.size(); i++) {
			int numero = i + 1;
			String bruta = texto.get(i);
			if (bruta.isBlank()) {
				continue;
			}
			Linha linha = lerLinha(numero, bruta, linhaDoCodigo, erros);
			if (linha != null) {
				linhas.add(linha);
			}
		}

		for (Linha linha : linhas) {
			for (String requisito : linha.preRequisitos()) {
				String codigo = Linha.codigoDe(requisito);
				boolean noArquivo = linhaDoCodigo.containsKey(codigo);
				if (Linha.externo(requisito) && noArquivo) {
					erros.add(DetalheErro.linha(linha.numero(), "pre_requisitos",
							codigo + " está no próprio arquivo: remova o prefixo " + PREFIXO_EXTERNO));
				}
				else if (!Linha.externo(requisito) && !noArquivo) {
					avisos.add(DetalheErro.linha(linha.numero(), "pre_requisitos",
							"Pré-requisito " + codigo + " não existe no arquivo"));
				}
			}
		}
		return new Resultado(List.copyOf(linhas), List.copyOf(erros), List.copyOf(avisos));
	}

	private static Linha lerLinha(int numero, String bruta, Map<String, Integer> linhaDoCodigo,
			List<DetalheErro> erros) {
		String[] campos = bruta.split(";", -1);
		if (campos.length != CABECALHO.size()) {
			erros.add(DetalheErro.linha(numero, "linha", "Esperadas " + CABECALHO.size()
					+ " colunas separadas por ';', encontradas " + campos.length));
			return null;
		}
		int errosAntes = erros.size();

		String codigo = campos[0].trim().toUpperCase(Locale.ROOT);
		if (!CODIGO.matcher(codigo).matches()) {
			erros.add(DetalheErro.linha(numero, "codigo", "Código vazio ou inválido"));
		}
		else if (linhaDoCodigo.containsKey(codigo)) {
			erros.add(DetalheErro.linha(numero, "codigo",
					"Código " + codigo + " repetido (já aparece na linha " + linhaDoCodigo.get(codigo) + ")"));
		}
		else {
			linhaDoCodigo.put(codigo, numero);
		}

		String nome = campos[1].trim();
		if (nome.isEmpty()) {
			erros.add(DetalheErro.linha(numero, "nome", "Nome vazio"));
		}

		Integer semestre = inteiro(campos[2], numero, "semestre", erros);
		if (semestre != null && (semestre < 1 || (semestre > 98 && semestre != SEMESTRE_OPTATIVAS))) {
			erros.add(DetalheErro.linha(numero, "semestre", "Semestre deve ser de 1 a 98, ou 99 para optativas"));
		}
		Integer creditos = inteiro(campos[3], numero, "creditos", erros);
		Integer cargaHoraria = inteiro(campos[4], numero, "carga_horaria", erros);

		Set<String> preRequisitos = new LinkedHashSet<>();
		for (String token : campos[5].split(",")) {
			String requisito = token.trim().toUpperCase(Locale.ROOT);
			if (requisito.isEmpty()) {
				continue;
			}
			String codigoRequisito = Linha.codigoDe(requisito);
			if (!CODIGO.matcher(codigoRequisito).matches()) {
				erros.add(DetalheErro.linha(numero, "pre_requisitos", "Pré-requisito inválido: " + token.trim()));
			}
			else if (codigoRequisito.equals(codigo)) {
				erros.add(DetalheErro.linha(numero, "pre_requisitos", "A disciplina não pode ser pré-requisito dela mesma"));
			}
			else {
				preRequisitos.add(requisito);
			}
		}

		Boolean optativa = simOuNao(campos[6]);
		if (optativa == null) {
			erros.add(DetalheErro.linha(numero, "optativa", "Use Sim ou Não"));
		}
		else if (semestre != null && optativa && semestre != SEMESTRE_OPTATIVAS) {
			erros.add(DetalheErro.linha(numero, "semestre", "Optativa usa o semestre 99"));
		}
		else if (semestre != null && !optativa && semestre == SEMESTRE_OPTATIVAS) {
			erros.add(DetalheErro.linha(numero, "optativa", "O semestre 99 é reservado às optativas"));
		}

		if (erros.size() > errosAntes) {
			return null;
		}
		return new Linha(numero, codigo, nome, semestre, creditos, cargaHoraria, List.copyOf(preRequisitos), optativa);
	}

	private static Integer inteiro(String valor, int numero, String campo, List<DetalheErro> erros) {
		try {
			int n = Integer.parseInt(valor.trim());
			if (n < 0) {
				erros.add(DetalheErro.linha(numero, campo, "Valor não pode ser negativo"));
				return null;
			}
			return n;
		}
		catch (NumberFormatException e) {
			erros.add(DetalheErro.linha(numero, campo, "Número inteiro esperado: '" + valor.trim() + "'"));
			return null;
		}
	}

	private static Boolean simOuNao(String valor) {
		return switch (valor.trim().toLowerCase(Locale.ROOT)) {
			case "sim" -> true;
			case "não", "nao" -> false;
			default -> null;
		};
	}
}
