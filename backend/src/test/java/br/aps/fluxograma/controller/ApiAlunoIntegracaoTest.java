package br.aps.fluxograma.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import br.aps.fluxograma.PostgresEmbutidoConfig;
import br.aps.fluxograma.config.DadosDesenvolvimento;

/**
 * Endpoints principais da fatia do aluno contra um PostgreSQL de verdade (embutido), com as
 * migrações Flyway e o seed de desenvolvimento (curso CC, fluxo 2023.1 do CSV de exemplo).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresEmbutidoConfig.class)
@ActiveProfiles("dev")
class ApiAlunoIntegracaoTest {

	private static final AtomicInteger SEQUENCIA = new AtomicInteger();

	@Autowired
	private MockMvc mvc;

	private record Sessao(long id, String token) {
	}

	@Test
	@DisplayName("HU22: cadastro, login por matrícula ou e-mail e erros no formato único")
	void cadastroELogin() throws Exception {
		String matricula = "2099" + SEQUENCIA.incrementAndGet();
		String email = "aluno" + matricula + "@teste.dev";

		mvc.perform(json(post("/auth/registro"), registro(matricula, "Aluno de Teste", email, "segredo1")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.token").isNotEmpty())
			.andExpect(jsonPath("$.tipo").value("Bearer"))
			.andExpect(jsonPath("$.usuario.papel").value("ALUNO"))
			.andExpect(jsonPath("$.usuario.matricula").value(matricula))
			.andExpect(jsonPath("$.usuario.fluxoId").doesNotExist());

		// Matrícula e e-mail já em uso → 409 com os dois campos
		ResultActions emUso = mvc
			.perform(json(post("/auth/registro"), registro(matricula, "Outro", email.toUpperCase(), "segredo1")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.erro").value("Matrícula e e-mail já estão em uso"));
		assertThat(lista(emUso, "$.detalhes[*].campo")).containsExactly("matricula", "email");

		// Dados inválidos → 400 com detalhes por campo
		ResultActions invalido = mvc.perform(json(post("/auth/registro"), registro("", "Nome", "nao-e-email", "123")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.erro").value("Dados inválidos"));
		assertThat(lista(invalido, "$.detalhes[*].campo")).contains("matricula", "email", "senha");

		mvc.perform(json(post("/auth/login"), corpoLogin(matricula, "segredo1"))).andExpect(status().isOk());
		mvc.perform(json(post("/auth/login"), corpoLogin(email.toUpperCase(), "segredo1"))).andExpect(status().isOk());

		// Credenciais inválidas não indicam qual campo está errado
		mvc.perform(json(post("/auth/login"), corpoLogin(matricula, "errada")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.erro").value("E-mail/matrícula ou senha inválidos"));
		mvc.perform(json(post("/auth/login"), corpoLogin("ninguem@teste.dev", "segredo1")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.erro").value("E-mail/matrícula ou senha inválidos"));

		mvc.perform(json(post("/auth/login"), "{ \"login\": "))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.erro").value("Requisição malformada"));
	}

	@Test
	@DisplayName("Sem token ou com token inválido → 401 no formato único")
	void semToken() throws Exception {
		mvc.perform(get("/cursos"))
			.andExpect(status().isUnauthorized())
			.andExpect(header().string("WWW-Authenticate", "Bearer"))
			.andExpect(jsonPath("$.erro").isNotEmpty());
		mvc.perform(get("/cursos").header("Authorization", "Bearer token-invalido"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.erro").isNotEmpty());
	}

	@Test
	@DisplayName("HU06: cursos e fluxos publicados com rótulo, semestres e obrigatórias")
	void cursosEFluxos() throws Exception {
		Sessao aluno = novoAluno();
		long cursoId = cursoCc(aluno);

		autenticado(get("/cursos/" + cursoId + "/fluxos"), aluno)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].nome").value("2023.1"))
			.andExpect(jsonPath("$[0].status").value("PUBLICADO"))
			.andExpect(jsonPath("$[0].rotulo").value("VIGENTE"))
			.andExpect(jsonPath("$[0].quantidadeSemestres").value(5))
			.andExpect(jsonPath("$[0].quantidadeObrigatorias").value(29))
			.andExpect(jsonPath("$[0].quantidadeOptativas").value(11))
			.andExpect(jsonPath("$[0].creditosObrigatorios").value(116))
			.andExpect(jsonPath("$[0].creditosOptativosExigidos").value(16))
			.andExpect(jsonPath("$[0].totalCreditosExigidos").value(132));

		autenticado(get("/cursos/999999/fluxos"), aluno).andExpect(status().isNotFound())
			.andExpect(jsonPath("$.erro").value("Curso não encontrado"));
	}

	@Test
	@DisplayName("HU08/HU09: fluxo completo na ordem do CSV e cadeia de pré-requisitos")
	void fluxoEDisciplina() throws Exception {
		Sessao aluno = novoAluno();
		long fluxoId = fluxo2023(aluno);

		ResultActions fluxo = autenticado(get("/fluxos/" + fluxoId), aluno).andExpect(status().isOk())
			.andExpect(jsonPath("$.semestres", hasSize(5)))
			.andExpect(jsonPath("$.semestres[1].disciplinas", hasSize(6)))
			.andExpect(jsonPath("$.optativas", hasSize(11)))
			.andExpect(jsonPath("$.semestres[0].disciplinas[0].estado").doesNotExist());
		assertThat(lista(fluxo, "$.semestres[0].disciplinas[*].codigo")).containsExactly("CC117", "CC118", "CC116",
				"CC119", "CC115");
		assertThat(lista(fluxo, "$.externas[*].codigo")).containsExactly("CT904", "CT911", "CC091");
		Map<String, Integer> ids = idsPorCodigo(fluxo.andReturn().getResponse().getContentAsString(), "$");

		ResultActions redes = autenticado(get("/disciplinas/" + ids.get("CC139")), aluno).andExpect(status().isOk())
			.andExpect(jsonPath("$.disciplina.codigo").value("CC139"));
		assertThat(lista(redes, "$.cadeiaAntes[*].codigo")).containsExactly("CC117", "CC122", "CC130", "CC134");
		assertThat(lista(redes, "$.cadeiaAntes[*].nivel")).containsExactly(4, 3, 2, 1);
		assertThat(lista(redes, "$.cadeiaDepois[*].codigo")).containsExactly("CC158");
	}

	@Test
	@DisplayName("HU06/HU07/HU18/HU19: selecionar fluxo, marcar e desmarcar com estados recalculados")
	void marcacoes() throws Exception {
		Sessao aluno = novoAluno();
		long fluxoId = fluxo2023(aluno);

		// Antes de escolher o fluxo
		autenticado(get("/alunos/" + aluno.id() + "/fluxograma"), aluno).andExpect(status().isNotFound());

		autenticado(json(put("/alunos/" + aluno.id() + "/fluxo"), "{\"fluxoId\": " + fluxoId + "}"), aluno)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.fluxoId").value(fluxoId));

		String inicial = fluxograma(aluno);
		Map<String, Integer> ids = idsPorCodigo(inicial, "$.fluxo");
		Map<String, String> estados = estadosPorCodigo(inicial);
		assertThat(estados).containsEntry("CC117", "DISPONIVEL")
			.containsEntry("CC154", "DISPONIVEL")
			.containsEntry("CL179", "DISPONIVEL")
			.containsEntry("CC122", "BLOQUEADA")
			.containsEntry("CT983", "BLOQUEADA");
		assertThat((Integer) JsonPath.read(inicial, "$.resumo.disponiveis")).isEqualTo(8);
		assertThat((Integer) JsonPath.read(inicial, "$.resumo.bloqueadas")).isEqualTo(32);
		assertThat((Integer) JsonPath.read(inicial, "$.resumo.percentualConcluido")).isZero();

		// Cursada libera quem depende dela
		marcar(aluno, ids.get("CC117"), "CURSADA").andExpect(status().isOk())
			.andExpect(jsonPath("$.situacao").value("CURSADA"));
		marcar(aluno, ids.get("CC117"), "CURSADA").andExpect(status().isOk()); // idempotente
		String aposCalculo = fluxograma(aluno);
		assertThat(estadosPorCodigo(aposCalculo)).containsEntry("CC117", "CURSADA")
			.containsEntry("CC122", "DISPONIVEL")
			.containsEntry("CC124", "DISPONIVEL");
		assertThat((Integer) JsonPath.read(aposCalculo, "$.resumo.percentualConcluido")).isEqualTo(3); // 4 de 132

		// Cursando não libera a próxima
		marcar(aluno, ids.get("CC122"), "CURSANDO").andExpect(status().isOk());
		assertThat(estadosPorCodigo(fluxograma(aluno))).containsEntry("CC122", "CURSANDO")
			.containsEntry("CC128", "BLOQUEADA");

		// Cursando exige os pré-requisitos cursados
		marcar(aluno, ids.get("CC128"), "CURSANDO").andExpect(status().isConflict())
			.andExpect(jsonPath("$.erro").value("Para marcar como cursando, os pré-requisitos precisam estar cursados: CC122"))
			.andExpect(jsonPath("$.detalhes[0].campo").value("preRequisitos"));

		// Desmarcar volta a bloquear o que dependia dela (idempotente)
		autenticado(delete("/alunos/" + aluno.id() + "/disciplinas/" + ids.get("CC117")), aluno)
			.andExpect(status().isNoContent());
		autenticado(delete("/alunos/" + aluno.id() + "/disciplinas/" + ids.get("CC117")), aluno)
			.andExpect(status().isNoContent());
		assertThat(estadosPorCodigo(fluxograma(aluno))).containsEntry("CC117", "DISPONIVEL")
			.containsEntry("CC124", "BLOQUEADA")
			.containsEntry("CC122", "CURSANDO");

		// Pré-requisito externo: só como cumprido (CURSADA)
		marcar(aluno, ids.get("CT904"), "CURSANDO").andExpect(status().isConflict());
		marcar(aluno, ids.get("CT904"), "CURSADA").andExpect(status().isOk());
		assertThat(estadosPorCodigo(fluxograma(aluno))).containsEntry("CT983", "DISPONIVEL")
			.containsEntry("CC083", "DISPONIVEL");

		// Situação inválida e disciplina de fora do fluxo
		autenticado(json(put("/alunos/" + aluno.id() + "/disciplinas/" + ids.get("CC118")), "{\"situacao\":\"FEITA\"}"),
				aluno)
			.andExpect(status().isBadRequest());
		marcar(aluno, 999999, "CURSADA").andExpect(status().isNotFound());

		// Com marcações, o fluxo não é mais trocado direto (HU06 → HU12/HU13)
		autenticado(json(put("/alunos/" + aluno.id() + "/fluxo"), "{\"fluxoId\": " + fluxoId + "}"), aluno)
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("Aluno só acessa os próprios dados; outros papéis recebem 403")
	void permissoes() throws Exception {
		Sessao ana = novoAluno();
		Sessao bruno = novoAluno();

		autenticado(get("/alunos/" + bruno.id() + "/fluxograma"), ana).andExpect(status().isForbidden())
			.andExpect(jsonPath("$.erro").isNotEmpty());
		autenticado(json(put("/alunos/" + bruno.id() + "/disciplinas/1"), "{\"situacao\":\"CURSADA\"}"), ana)
			.andExpect(status().isForbidden());

		Sessao coordenador = login(DadosDesenvolvimento.COORDENADOR_EMAIL, DadosDesenvolvimento.COORDENADOR_SENHA);
		autenticado(get("/alunos/" + ana.id() + "/fluxograma"), coordenador).andExpect(status().isForbidden())
			.andExpect(jsonPath("$.erro").value("Acesso negado"));
		autenticado(get("/cursos/" + cursoCc(coordenador) + "/fluxos"), coordenador).andExpect(status().isOk())
			.andExpect(jsonPath("$[0].nome").value("2023.1"));
	}

	@Test
	@DisplayName("Seed: aluno de demonstração com os semestres 1 a 4 cursados")
	void alunoDeDemonstracao() throws Exception {
		Sessao demo = login(DadosDesenvolvimento.ALUNO_MATRICULA, DadosDesenvolvimento.ALUNO_SENHA);

		String corpo = fluxograma(demo);
		assertThat((String) JsonPath.read(corpo, "$.aluno.nome")).isEqualTo(DadosDesenvolvimento.ALUNO_NOME);
		assertThat((Integer) JsonPath.read(corpo, "$.resumo.cursadas")).isEqualTo(23);
		assertThat((Integer) JsonPath.read(corpo, "$.resumo.creditosConcluidos")).isEqualTo(92);
		assertThat((Integer) JsonPath.read(corpo, "$.resumo.percentualConcluido")).isEqualTo(69); // 92 de 132
		assertThat(estadosPorCodigo(corpo)).containsEntry("CC141", "DISPONIVEL").containsEntry("CC139", "DISPONIVEL");
	}

	// --- apoio ---

	private Sessao novoAluno() throws Exception {
		String matricula = "2098" + SEQUENCIA.incrementAndGet();
		String corpo = mvc
			.perform(json(post("/auth/registro"),
					registro(matricula, "Aluno " + matricula, "a" + matricula + "@teste.dev", "senha123")))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return sessao(corpo);
	}

	private Sessao login(String login, String senha) throws Exception {
		String corpo = mvc.perform(json(post("/auth/login"), corpoLogin(login, senha)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return sessao(corpo);
	}

	private static Sessao sessao(String corpo) {
		Number id = JsonPath.read(corpo, "$.usuario.id");
		return new Sessao(id.longValue(), JsonPath.read(corpo, "$.token"));
	}

	private long cursoCc(Sessao sessao) throws Exception {
		String corpo = autenticado(get("/cursos"), sessao).andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		List<Number> ids = JsonPath.read(corpo, "$[?(@.codigo == 'CC')].id");
		return ids.get(0).longValue();
	}

	private long fluxo2023(Sessao sessao) throws Exception {
		String corpo = autenticado(get("/cursos/" + cursoCc(sessao) + "/fluxos"), sessao).andReturn()
			.getResponse()
			.getContentAsString();
		List<Number> ids = JsonPath.read(corpo, "$[?(@.nome == '2023.1')].id");
		return ids.get(0).longValue();
	}

	private String fluxograma(Sessao aluno) throws Exception {
		return autenticado(get("/alunos/" + aluno.id() + "/fluxograma"), aluno).andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
	}

	private ResultActions marcar(Sessao aluno, long disciplinaId, String situacao) throws Exception {
		return autenticado(json(put("/alunos/" + aluno.id() + "/disciplinas/" + disciplinaId),
				"{\"situacao\":\"" + situacao + "\"}"), aluno);
	}

	private ResultActions autenticado(MockHttpServletRequestBuilder requisicao, Sessao sessao) throws Exception {
		return mvc.perform(requisicao.header("Authorization", "Bearer " + sessao.token()));
	}

	private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder requisicao, String corpo) {
		return requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
	}

	private static List<Object> lista(ResultActions resposta, String caminho) throws Exception {
		return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), caminho);
	}

	private static List<Map<String, Object>> disciplinas(String corpo, String raiz) {
		List<Map<String, Object>> todas = new ArrayList<>();
		todas.addAll(JsonPath.read(corpo, raiz + ".semestres[*].disciplinas[*]"));
		todas.addAll(JsonPath.read(corpo, raiz + ".optativas[*]"));
		todas.addAll(JsonPath.read(corpo, raiz + ".externas[*]"));
		return todas;
	}

	private static Map<String, Integer> idsPorCodigo(String corpo, String raiz) {
		Map<String, Integer> ids = new HashMap<>();
		for (Map<String, Object> d : disciplinas(corpo, raiz)) {
			ids.put((String) d.get("codigo"), ((Number) d.get("id")).intValue());
		}
		return ids;
	}

	private static Map<String, String> estadosPorCodigo(String fluxograma) {
		Map<String, String> estados = new HashMap<>();
		for (Map<String, Object> d : disciplinas(fluxograma, "$.fluxo")) {
			estados.put((String) d.get("codigo"), (String) d.get("estado"));
		}
		return estados;
	}

	private static String registro(String matricula, String nome, String email, String senha) {
		return """
				{"matricula": "%s", "nome": "%s", "email": "%s", "senha": "%s"}
				""".formatted(matricula, nome, email, senha);
	}

	private static String corpoLogin(String login, String senha) {
		return """
				{"login": "%s", "senha": "%s"}
				""".formatted(login, senha);
	}
}
