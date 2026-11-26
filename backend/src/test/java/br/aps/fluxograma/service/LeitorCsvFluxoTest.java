package br.aps.fluxograma.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import br.aps.fluxograma.erro.DetalheErro;
import br.aps.fluxograma.service.LeitorCsvFluxo.Linha;

class LeitorCsvFluxoTest {

	private static final String CABECALHO = "codigo;nome;semestre;creditos;carga_horaria;pre_requisitos;optativa\n";

	@Test
	@DisplayName("Lê o CSV de exemplo da documentação (docs/exemplos/modelo-fluxo.csv) sem erros")
	void exemploDaDocumentacao() throws IOException {
		LeitorCsvFluxo.Resultado resultado;
		try (Reader leitor = new InputStreamReader(new ClassPathResource("dados-dev/modelo-fluxo.csv").getInputStream(),
				StandardCharsets.UTF_8)) {
			resultado = LeitorCsvFluxo.ler(leitor);
		}

		assertThat(resultado.erros()).isEmpty();
		assertThat(resultado.avisos()).isEmpty();
		assertThat(resultado.linhas()).hasSize(40);
		assertThat(resultado.linhas()).filteredOn(Linha::optativa).hasSize(11);
		assertThat(resultado.linhas()).filteredOn(l -> l.semestre() == 1)
			.extracting(Linha::codigo)
			.containsExactly("CC117", "CC118", "CC116", "CC119", "CC115");
		Linha padroes = resultado.linhas().stream().filter(l -> l.codigo().equals("CT983")).findFirst().orElseThrow();
		assertThat(padroes.preRequisitos()).containsExactly("EXT:CT904");
		Linha algebra = resultado.linhas().stream().filter(l -> l.codigo().equals("CC123")).findFirst().orElseThrow();
		assertThat(algebra).extracting(Linha::nome, Linha::semestre, Linha::creditos, Linha::cargaHoraria)
			.containsExactly("Álgebra Linear para Computação", 2, 4, 68);
		assertThat(algebra.preRequisitos()).containsExactly("CC118");
	}

	@Test
	@DisplayName("Vários pré-requisitos separados por vírgula")
	void variosPreRequisitos() {
		LeitorCsvFluxo.Resultado resultado = LeitorCsvFluxo.ler(CABECALHO
				+ "A1;Disciplina A;1;4;68;;Não\nB1;Disciplina B;1;4;68;;Não\nC2;Disciplina C;2;4;68;A1, B1 ,EXT:X9;Não\n");

		assertThat(resultado.valido()).isTrue();
		assertThat(resultado.linhas().get(2).preRequisitos()).containsExactly("A1", "B1", "EXT:X9");
	}

	@Test
	@DisplayName("Cabeçalho errado rejeita o arquivo")
	void cabecalhoErrado() {
		LeitorCsvFluxo.Resultado resultado = LeitorCsvFluxo.ler("codigo;nome;semestre\nA1;A;1\n");

		assertThat(resultado.valido()).isFalse();
		assertThat(resultado.erros()).extracting(DetalheErro::linha, DetalheErro::campo).containsExactly(tuple(1, "cabecalho"));
	}

	@Test
	@DisplayName("Erros de formato informam linha e campo")
	void errosDeFormato() {
		String csv = CABECALHO
				+ "A1;Disciplina A;1;4;68;;Não\n"     // 2: ok
				+ "B1;Disciplina B;um;4;68;;Não\n"    // 3: semestre não numérico
				+ "C1;Disciplina C;1;4;68;;Talvez\n"  // 4: optativa inválida
				+ "D1;Disciplina D;3;4;68;;Sim\n"     // 5: optativa fora do 99
				+ "E1;Disciplina E;99;4;68;;Não\n"    // 6: 99 sem ser optativa
				+ "A1;Repetida;1;4;68;;Não\n"         // 7: código repetido
				+ "F1;Disciplina F;1;4;68\n"          // 8: colunas faltando
				+ "G1;;1;4;-2;;Não\n";                // 9: nome vazio e carga negativa

		LeitorCsvFluxo.Resultado resultado = LeitorCsvFluxo.ler(csv);

		assertThat(resultado.valido()).isFalse();
		assertThat(resultado.erros()).extracting(DetalheErro::linha, DetalheErro::campo)
			.containsExactly(tuple(3, "semestre"), tuple(4, "optativa"), tuple(5, "semestre"), tuple(6, "optativa"),
					tuple(7, "codigo"), tuple(8, "linha"), tuple(9, "nome"), tuple(9, "carga_horaria"));
	}

	@Test
	@DisplayName("Pré-requisito fora do arquivo sem EXT: é aviso, não erro (vira externa pendente)")
	void preRequisitoNaoEncontrado() {
		LeitorCsvFluxo.Resultado resultado = LeitorCsvFluxo.ler(CABECALHO + "A1;Disciplina A;1;4;68;;Não\n"
				+ "B2;Disciplina B;2;4;68;A1,CC999;Não\n");

		assertThat(resultado.valido()).isTrue();
		assertThat(resultado.avisos()).extracting(DetalheErro::linha, DetalheErro::campo, DetalheErro::mensagem)
			.containsExactly(tuple(3, "pre_requisitos", "Pré-requisito CC999 não existe no arquivo"));
	}

	@Test
	@DisplayName("EXT: num código que está no próprio arquivo é erro")
	void externoQueEstaNoArquivo() {
		LeitorCsvFluxo.Resultado resultado = LeitorCsvFluxo.ler(CABECALHO + "A1;Disciplina A;1;4;68;;Não\n"
				+ "B2;Disciplina B;2;4;68;EXT:A1;Não\n");

		assertThat(resultado.erros()).extracting(DetalheErro::linha, DetalheErro::campo)
			.containsExactly(tuple(3, "pre_requisitos"));
	}

	@Test
	@DisplayName("Aceita BOM do UTF-8, linhas em branco e 'Nao' sem acento")
	void tolerancias() {
		LeitorCsvFluxo.Resultado resultado = LeitorCsvFluxo
			.ler("﻿" + CABECALHO + "\nA1;Disciplina A;1;4;68;;Nao\n\n");

		assertThat(resultado.valido()).isTrue();
		assertThat(resultado.linhas()).hasSize(1);
	}
}
