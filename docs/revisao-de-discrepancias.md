# Revisão de discrepâncias — novembro de 2026

Cruzamento entre as três fontes do projeto antes do início do desenvolvimento:

- **HU** — Histórias de Usuário entregues ([originais/historias-de-usuario.docx](originais/historias-de-usuario.docx), HU01 a HU19);
- **Arq.** — Documento de Arquitetura entregue ([originais/documento-de-arquitetura.pdf](originais/documento-de-arquitetura.pdf));
- **Figma** — protótipo [Fluxograma Acadêmico](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico) (19 telas).

Os documentos entregues **não foram alterados**. As decisões abaixo valem para o desenvolvimento e já estão aplicadas em:

- [historias-de-usuario.md](historias-de-usuario.md) — backlog consolidado (HU01 a HU22);
- [arquitetura.md](arquitetura.md) — arquitetura, modelo de dados e API consolidados;
- [prototipo-figma.md](prototipo-figma.md) — mapa de telas por HU e telas que ainda faltam.

Também foi considerado o comentário do professor sobre o protótipo: o coordenador precisa incluir o fluxo e **ajustar o fluxograma quando os semestres não têm 5 ou 6 disciplinas certinhas e várias ficam lado a lado no mesmo semestre** (ver D1).

**Situação:** ✅ corrigido no Figma · 📘 resolvido na especificação (HU/arquitetura consolidadas) · 🟡 falta desenhar no Figma antes de implementar a tela.

## Resumo

| Grupo | Itens | Situação |
|---|---|---|
| A. HU × Arquitetura | 12 | 📘 todos resolvidos na especificação |
| B. Figma × HU/Arquitetura (funcionalidades) | 18 | 📘 resolvidos; parte também ✅ no Figma; 6 dependem de tela nova 🟡 |
| C. Dados inconsistentes dentro do Figma | 8 | ✅ corrigidos no Figma |
| D. Telas que faltam no Figma | 12 | ✅ D1, D2, D3, D8 e D11 desenhadas · D5 e D7 em parte · 🟡 D4, D6, D9, D10 e D12 pendentes (lista em [prototipo-figma.md](prototipo-figma.md#telas-que-faltam)) |

---

## A. Histórias de Usuário × Documento de Arquitetura

| # | Discrepância | Decisão para o desenvolvimento | Situação |
|---|---|---|---|
| A1 | A tabela de atores das HU diz que o Admin Institucional "faz o que o coordenador faz", mas a Arq. (3.5) diz que importar fluxo e cadastrar oferta são **exclusivos** do coordenador. | O admin pode fazer tudo o que o coordenador faz, em **qualquer** curso; o coordenador fica restrito ao próprio curso. | 📘 |
| A2 | `POST /auth/registro` cadastra o aluno só com matrícula, nome e senha, mas `usuario.email` é identificador (único, obrigatório) no DER. | O cadastro do aluno pede matrícula, nome, **e-mail** e senha. O login aceita e-mail ou matrícula. | 📘 |
| A3 | Login e cadastro existem na Arq. (seção 5.1), mas nenhuma HU os cobre. | Criada a **HU22 — Criar conta e acessar o sistema**. | 📘 |
| A4 | HU03 pede "quantas optativas (**ou** quantos créditos optativos)"; a Arq. guarda `optativas_exigidas` (quantidade). O Figma trabalha em créditos ("faltam 16 créditos de optativas para atingir os 194 exigidos"). | O fluxo guarda `creditos_optativos_exigidos` (em créditos). Total exigido = créditos das obrigatórias + créditos optativos (no fluxo 2023.1: 178 + 16 = 194). HU03 e HU15 ajustadas. | 📘 |
| A5 | O DER exige que todo ALUNO siga exatamente um FLUXO, mas o aluno se cadastra antes de escolher o fluxo (HU06). | ALUNO segue **0..1** FLUXO; `aluno.fluxo_id` é anulável até a HU06. | 📘 |
| A6 | O texto diz "notação de Heuser", mas as cardinalidades estão posicionadas na convenção (mín,máx) de participação. Lido como Heuser, o DER diz o contrário do pretendido (ex.: cada curso teria exatamente 1 coordenador e cada coordenador coordenaria 0..n cursos, contrariando a HU01 e a coluna `coordenador.curso_id`). | DER redesenhado na convenção de Heuser, com a regra de leitura na legenda ([img/der-heuser.svg](img/der-heuser.svg)). | 📘 |
| A7 | HU16 diz "escolher outra **turma**", mas HU05 e Arq. excluem turmas paralelas da mesma disciplina. | Texto corrigido para "escolher outra disciplina". Turmas continuam fora desta versão. | 📘 |
| A8 | O exemplo de cadeia "TALF → Teoria da Computação → Compiladores" (HU glossário e HU09) não corresponde à matriz do protótipo (a disciplina é "Teoria da Computabilidade" e a cadeia de Compiladores não aparece). O exemplo de erro da Arq. usa a sigla "TALF", mas o CSV trabalha com códigos. | Exemplo trocado por uma cadeia que existe no Figma: Probabilidade e Estatística → Avaliação de Desempenho → Redes de Computadores → Sistemas Distribuídos. Exemplo de erro com código (`CC999`). | 📘 |
| A9 | Glossário cita "Fluxo 2015, Fluxo 2022"; o Figma usa "Fluxo 2015.1" e "Fluxo 2023.1". | Nome do fluxo = ano e semestre de início (ex.: 2023.1). | 📘 |
| A10 | A oferta é salva de uma vez por período ("Salvar oferta do período", HU05), mas a API só tinha criação e edição de ofertas uma a uma. | `PUT /periodos-matricula/{id}/fluxos/{fluxoId}/ofertas` substitui o conjunto de ofertas do fluxo no período. | 📘 |
| A11 | O aluno precisa saber se há período de matrícula aberto (HU14), mas a listagem de períodos estava só na área do admin. | `GET /cursos/{id}/periodos-matricula` liberado para todos os papéis (com filtro `?aberto=true`). | 📘 |
| A12 | Estado "Período atual: 2026.2" no Figma com 0 disciplinas cursando e o mesmo semestre sendo simulado — o termo "atual" não estava definido. | "Semestre letivo atual" = o do período de matrícula aberto ou, se não houver, o do último período já iniciado. O 1º semestre simulado é esse semestre. | 📘 |

## B. Protótipo (Figma) × HU / Arquitetura

| # | Discrepância | Decisão para o desenvolvimento | Situação |
|---|---|---|---|
| B1 | O Figma tem **editor visual de fluxo** (Novo fluxo, Editor de fluxo, "CSV opcional", duplicar fluxo, arrastar cards entre semestres, qualquer quantidade por semestre). A HU03 só previa importar CSV e a API só tinha importação. É exatamente o ponto do comentário do professor. | HU03 reescrita (criar do zero, duplicando ou por CSV) e **HU20 — Ajustar o fluxograma no editor visual** criada. Modelo ganhou SEMESTRE DO FLUXO (com título), `disciplina.ordem` (posição lado a lado no semestre) e API de semestres, disciplinas, avisos e publicação. | 📘 |
| B2 | Status de fluxo no Figma (Rascunho, Publicar, "Vigente", "Em acompanhamento", "prévia") sem modelagem. | `fluxo.status` (RASCUNHO, PUBLICADO) + `fluxo.vigencia_inicio`. Prévia, Vigente e Em acompanhamento são calculados. Rascunho só a coordenação vê. | 📘 |
| B3 | Carga horária (68 h, 102 h, 204 h, "3.026 h no total") aparece e é editável no Figma, mas não existe nas HU, no DER nem no CSV. | `disciplina.carga_horaria` armazenada; coluna `carga_horaria` no CSV (texto do Figma atualizado). | 📘 ✅ |
| B4 | O Figma mostra **pré-requisitos externos** (CT904, CT911, CC091); a HU03 rejeita pré-requisito que não está no arquivo e o modelo só aceita pré-requisito que seja disciplina cadastrada. | Disciplina com `tipo = EXTERNA` (fora do fluxo, sem semestre, não conta no progresso). No CSV, prefixo `EXT:` (ex.: `EXT:CT904`). O aluno informa que cumpriu o pré-requisito externo no painel de detalhes. | 📘 🟡 (D4) |
| B5 | Optativas no Figma ficam num "catálogo · semestre 99, sem ordem fixa"; nada disso estava descrito. | Glossário e modelo: optativas não têm semestre (no CSV usam 99) e aparecem em grade separada, sem setas. | 📘 |
| B6 | Tela **Previsão de formatura** sem HU e sem endpoint. | Criada a **HU21** e `GET /planejamentos/{id}/previsao`. | 📘 |
| B7 | "Meus planejamentos" tem vários planos e um **Plano principal** "Em uso"; o modelo não distinguia o principal nem guardava a última edição. | `planejamento.principal` (no máximo um por aluno), `semestre_inicial` e `atualizado_em`. HU11 ampliada. | 📘 🟡 (D9) |
| B8 | Ofertas com **carga flexível** e "Coordenação de extensão/do curso" no lugar de professor; o DER exigia 1..n blocos de horário por oferta. | `oferta.carga_flexivel`; blocos de horário 0..n (zero só com carga flexível); `professor` aceita professor ou responsável. Carga flexível não gera conflito (HU16). | 📘 |
| B9 | HU08 pede setas sempre visíveis; o Figma mostra setas **só na disciplina selecionada** (modo foco) e contadores ↑/↓ nos cards, além de busca por nome/código. | HU08 e HU09 alinhadas ao Figma. | 📘 |
| B10 | Simulação de matrícula listava só "6 disciplinas disponíveis" (as do 5º semestre), contrariando a HU10 (todas as disponíveis) e a HU15 (optativas). | Figma: "6 de 21 disponíveis · filtro: 5º semestre". HU10 explicita o filtro por semestre. | 📘 ✅ |
| B11 | Matrícula do período dizia "Plano principal · Semestre 5", mas a seleção era diferente da do Plano principal na simulação. | Figma passa a citar o plano "Caminho com optativas". HU14: a matrícula parte de um planejamento salvo (o principal por padrão) e o aluno pode trocar o plano usado. | 📘 ✅ 🟡 (D7) |
| B12 | Botão "Concluir matrícula" sugeria matrícula oficial, o que o sistema não faz. | Renomeado para "Confirmar seleção"; HU16 define que confirmar só salva o semestre no planejamento, e só sem conflitos. | 📘 ✅ |
| B13 | Simulação de troca de fluxo: não havia como **confirmar a troca** (HU13) e o texto dizia que "a equivalência definitiva depende da validação da coordenação", contrariando a HU13. | Textos corrigidos ("as equivalências aplicadas são as cadastradas pela coordenação"; "a troca só acontece se você confirmar"; "Todas as 23 concluídas têm equivalência"). Falta o botão de confirmação. | 📘 ✅ 🟡 (D6) |
| B14 | HU06 pede escolher o curso antes do fluxo; a tela "Selecionar fluxo" vai direto aos fluxos. | HU06: com um único curso (caso desta versão), a escolha de curso é pulada. | 📘 |
| B15 | O fluxo 2027.1 aparece como "prévia" na troca, mas não na seleção inicial. | Regra: fluxos em prévia aparecem só na simulação de troca (HU06/HU12). | 📘 |
| B16 | Login: "Use as credenciais da sua instituição" (sugere login institucional), "alunos e coordenadores" (sem admin), "Esqueci minha senha" sem HU/endpoint e nenhum caminho para o aluno criar conta. | Textos corrigidos no Figma; o link vira "Primeiro acesso? Criar conta". Recuperação de senha fica fora desta versão. Falta a tela de cadastro. | 📘 ✅ 🟡 (D1) |
| B17 | O aluno tem duas navegações diferentes: barra lateral na Matriz (sem Previsão) e barra superior nas demais telas (sem Matriz, Matrícula e Troca). | Uma navegação única com todas as áreas do aluno. | 🟡 (D10) |
| B18 | O rótulo "↑ externo" usava vermelho (#A2443D) em disciplinas a cursar, contra a HU08. | Trocado para o neutro do protótipo (#6B6558) nas telas Matriz curricular, Detalhes de disciplina e Modo foco. | ✅ |

## C. Dados inconsistentes dentro do Figma (todos corrigidos)

| # | Tela | Antes | Depois |
|---|---|---|---|
| C1 | Marcar disciplinas cursadas | "8 disciplinas marcadas e 16 ainda por cursar" (a própria tela mostra 23 de 44) | "23 disciplinas marcadas e 21 ainda por cursar" |
| C2 | Meus planejamentos | "228 / 240 cr" (2×), "240 / 240 cr", "46 disciplinas" — a Previsão usa 194 créditos exigidos | "178 / 194 cr" (2×), "194 / 194 cr", "48 disciplinas" |
| C3 | Simulação de matrícula | "3 disciplinas ficariam disponíveis", mas a seleção (APS, Extensão I, IC, PCP, Redes) libera 5 | "5 disciplinas"; cards CC202 (após IC) e CC159 (após APS) adicionados |
| C4 | Matrícula do período | CL179 com "4 cr" (no resto do protótipo: 204 h = 12 cr) | "12 cr" |
| C5 | Cadastrar oferta do período | Horários e responsáveis de CL179, CL832, CC201 e CC111 diferentes dos da Matrícula do período (que mostra o conflito CC201 × CC111) | CL179 e CL832 com carga flexível e coordenações; CC201 TER/QUI 14:00–16:00 (Profa. Marina Azevedo); CC111 TER/QUI 15:00–17:00 (Prof. Lucas Ferreira) |
| C6 | Cadastrar oferta do período | Rodapé "61 de 61 · 26 ofertadas · 35 não ofertadas" (são 56 disciplinas, 18 ofertadas) | "56 de 56 · 18 ofertadas · 38 não ofertadas"; nota de validação cita carga flexível e responsável |
| C7 | Importar CSV | Erro sugeria a correção exata ("Corrija para CC132…"), o que o sistema não tem como saber; sem coluna de carga horária | Mensagem neutra ("Nenhuma disciplina foi cadastrada ainda.") e coluna `carga_horaria` na lista |
| C8 | Editor de fluxo | Métrica de optativas sem os créditos exigidos | "catálogo · 16 cr exigidos" |

Conferido no Figma (somas): 44 obrigatórias = 43 × 4 cr + Estágio 6 cr = 178 cr = 3.026 h; 23 cursadas = 92 cr = 1.564 h; 92 + 86 planejados + 16 optativos pendentes = 194.

## D. Telas que faltam no Figma

Sem estas telas as HUs correspondentes não têm base visual para o front-end. A especificação já define o comportamento. As telas novas estão na página **Revisão nov/2026 — telas novas** do Figma; os campos acrescentados a telas existentes estão na própria tela, na Page 1. Links e detalhes em [prototipo-figma.md](prototipo-figma.md#telas-que-faltam).

| # | Tela | HU | Situação |
|---|---|---|---|
| D1 | Criar conta (aluno) | HU22 | ✅ desenhada |
| D2 | Admin: cursos e coordenadores | HU01 | ✅ desenhadas (Admin · Cursos e Admin · Coordenadores do curso) |
| D3 | Admin: períodos de matrícula | HU02 | ✅ desenhada |
| D4 | Painel de detalhes com ações: marcar cursada, marcar cursando, desmarcar, "já cumpri este pré-requisito externo" | HU09, HU18, HU19 | 🟡 pendente |
| D5 | Horários indisponíveis | HU17 | ✅ cadastro desenhado · 🟡 falta o aviso de conflito na Matrícula do período |
| D6 | Simulação de troca: botão "Confirmar troca" e confirmação com as cursadas sem equivalência | HU13 | 🟡 pendente |
| D7 | Matrícula do período: estado "fora do período" e escolha do planejamento usado | HU14 | ✅ estado "fora do período" · 🟡 falta o seletor do planejamento |
| D8 | Novo fluxo: campo "Créditos optativos exigidos" | HU03 | ✅ incluído na tela existente |
| D9 | Meus planejamentos: definir como principal, renomear, remover | HU11 | 🟡 pendente |
| D10 | Navegação única do aluno | — | 🟡 pendente |
| D11 | Painel do coordenador listando também o fluxo 2027.1 (rascunho/prévia) | HU03, HU20 | ✅ incluído na tela existente |
| D12 | Importar CSV: coluna "Carga horária" na pré-visualização | HU03 | 🟡 pendente |
