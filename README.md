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
- **Ambiente:** Docker Compose

## Situação

Especificação revisada e alinhada com o protótipo (novembro de 2026). Próximo passo: estrutura inicial do back-end e do front-end. As telas que faltavam para HU01, HU02, HU17 (cadastro de horários) e HU22 (criar conta) já foram desenhadas. Antes de implementar as ações do painel de detalhes (HU09, HU18, HU19), a confirmação de troca (HU13), as ações de Meus planejamentos (HU11), o seletor de planejamento da matrícula (HU14) e a coluna de carga horária do CSV (HU03), desenhar o que ainda está pendente em [Telas que faltam](docs/prototipo-figma.md#telas-que-faltam).

## Equipe

- Lucas Emanuel Rodrigues de Matos
- Maria de Lourdes Melo Linhares
- Murilo Albuquerque Ferraz
- Francisco Robson de Oliveira Dutra Filho
- Lucas Almeida Silveira de Souza
