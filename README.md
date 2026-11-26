# APS — Fluxograma Interativo para Planejamento Acadêmico

Projeto da disciplina **Análise e Projeto de Software** (Ciência da Computação). Cliente: Alicia Paiva.

O SISACAD mostra a matriz curricular como um fluxograma estático. Este sistema torna o fluxograma interativo: o aluno acompanha o próprio progresso, vê a cadeia de pré-requisitos de cada disciplina, simula planejamentos de matrícula semestre a semestre até a formatura e testa a troca de fluxo. O coordenador monta e ajusta os fluxos (matrizes curriculares) num editor visual, cadastra equivalências e a oferta de cada período; o admin institucional cuida de cursos, coordenadores e períodos de matrícula.

Nesta versão o sistema não se integra ao SISACAD: o aluno informa o próprio histórico e a "matrícula" é só o planejamento, nunca a matrícula oficial.

## Documentação

| Documento | Conteúdo |
|---|---|
| [docs/historias-de-usuario.md](docs/historias-de-usuario.md) | Backlog consolidado: atores, glossário e HU01 a HU22 com critérios de aceitação |
| [docs/arquitetura.md](docs/arquitetura.md) | Arquitetura, modelo de dados (DER em notação de Heuser e esquema relacional), formato do CSV, API REST e regras de cálculo |
| [docs/prototipo-figma.md](docs/prototipo-figma.md) | Telas do protótipo por HU e telas que ainda faltam desenhar |
| [docs/revisao-de-discrepancias.md](docs/revisao-de-discrepancias.md) | Discrepâncias encontradas entre HUs, arquitetura e Figma, e a decisão tomada para cada uma |
| [docs/originais/](docs/originais/) | Histórias de Usuário e Documento de Arquitetura como foram entregues |

Os arquivos em `docs/` (fora de `originais/`) são a referência para o desenvolvimento: onde diferirem dos documentos entregues, valem eles.

Protótipo: [Fluxograma Acadêmico no Figma](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico).

## Stack

- **Front-end:** React (SPA)
- **Back-end:** Java + Spring Boot (Spring MVC, Spring Data JPA, Spring Security com JWT, springdoc-openapi)
- **Banco de dados:** PostgreSQL
- **Ambiente:** Docker Compose (depois do MVP; o MVP roda sem Docker)

## Situação

Especificação revisada e alinhada com o protótipo (novembro de 2026). Primeira entrega: o MVP da jornada do aluno, no branch [`mvp-aluno`](https://github.com/Lucas4lmeida/APS/tree/mvp-aluno), que roda sem Docker (instruções no README desse branch; escopo na [arquitetura](docs/arquitetura.md#7-entregas)). As telas que faltavam para HU01, HU02, HU17 (cadastro de horários) e HU22 (criar conta) já foram desenhadas. As ações do painel de detalhes (D4) e a navegação única do aluno (D10) já estão no MVP, com os nomes definidos em [Telas que faltam](docs/prototipo-figma.md#telas-que-faltam), e ainda precisam ser desenhadas no Figma. Antes de implementar a confirmação de troca (HU13), as ações de Meus planejamentos (HU11), o seletor de planejamento da matrícula (HU14) e a coluna de carga horária do CSV (HU03), desenhar o que ainda está pendente em [Telas que faltam](docs/prototipo-figma.md#telas-que-faltam).

## Equipe

- Lucas Emanuel Rodrigues de Matos
- Maria de Lourdes Melo Linhares
- Murilo Albuquerque Ferraz
- Francisco Robson de Oliveira Dutra Filho
- Lucas Almeida Silveira de Souza

## Como rodar (MVP, sem Docker)

O MVP cobre a jornada do aluno: criar conta e entrar (HU22), escolher o fluxo (HU06), ver a matriz com os estados e o resumo (HU08), marcar as disciplinas já cursadas (HU07), abrir os detalhes com a cadeia de pré-requisitos em modo foco (HU09) e marcar ou desmarcar cursando e cursada (HU18, HU19).

Precisa de **Java 21** e **Node.js 20 ou mais novo**. Não precisa de Docker, Maven nem PostgreSQL instalados.

**1. API** (num terminal):

```bash
cd backend
./mvnw spring-boot:test-run      # no Windows: mvnw.cmd spring-boot:test-run
```

Sobe em http://localhost:8080 com um PostgreSQL embutido. Os dados ficam em `~/.aps-fluxograma/pg-dados` (apague a pasta para recomeçar do zero). Documentação interativa da API: http://localhost:8080/swagger-ui.

Para usar um PostgreSQL próprio (banco `aps`, usuário `aps`, senha `aps`, ou as variáveis `DB_URL`, `DB_USUARIO` e `DB_SENHA`): `./mvnw spring-boot:run`.

**2. Front-end** (em outro terminal):

```bash
cd frontend
npm install
npm run dev
```

Abra http://localhost:5173. O Vite repassa as chamadas `/api` para a API na porta 8080.

**Contas de desenvolvimento** (criadas só no perfil `dev`, que é o padrão; nunca use em produção):

| Papel | Login | Senha |
|---|---|---|
| Aluna de demonstração (Ana Costa, semestres 1 a 4 cursados) | `2023100001` ou `ana.costa@aps.dev` | `aluno123` |
| Coordenador | `coordenacao.cc@aps.dev` | `coord123` |
| Admin institucional | `admin@aps.dev` | `admin123` |

**Testes:** `./mvnw verify` em `backend/` (unitários e de integração, com o PostgreSQL embutido) e `npm run build` em `frontend/` (checagem de tipos e build).

**Fora do MVP (próximos passos):** telas do coordenador e do admin (HU01 a HU05, HU20), simulação, planejamentos e previsão de formatura (HU10, HU11, HU21), troca de fluxo (HU12, HU13), matrícula do período e horários (HU14 a HU17) e a dockerização com Docker Compose prevista na [arquitetura](docs/arquitetura.md).
