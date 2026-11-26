# Histórias de Usuário

**Sistema de Fluxograma Interativo para Planejamento Acadêmico**  
Disciplina: Análise e Projeto de Software · Cliente: Alicia Paiva

> **Backlog consolidado para o desenvolvimento.** Incorpora as correções de [revisao-de-discrepancias.md](revisao-de-discrepancias.md) sobre o documento entregue ([originais/historias-de-usuario.docx](originais/historias-de-usuario.docx)). Onde houver diferença, vale este arquivo. HU20 a HU22 foram criadas nesta revisão; o mapa de telas está em [prototipo-figma.md](prototipo-figma.md).

## 1. Descrição do Sistema

O SISACAD atual apresenta um fluxograma estático da matriz curricular: caixas com nome, código e pré-requisito de cada disciplina, conectadas por setas indicando o que cada uma desbloqueia. O sistema proposto torna esse fluxograma interativo, permitindo ao aluno visualizar seu progresso real no curso e simular planejamentos de matrícula semestre a semestre, até a formatura, com base nas disciplinas que pretende cursar.

O sistema não se integra ao SISACAD nesta versão — os dados de curso do aluno (fluxo e disciplinas já cursadas) são informados manualmente por ele, e a “matrícula” no sistema é apenas o planejamento da matrícula do período, nunca a matrícula oficial. O sistema pode atender a mais de um curso, mas esta versão considera apenas o curso de Computação.

## 2. Atores

| Ator | Descrição |
|---|---|
| Aluno | Cria sua conta, seleciona o fluxo, marca disciplinas cursadas e cursando, visualiza seu progresso e simula planejamentos de matrícula, trocas de fluxo e a previsão de formatura |
| Coordenador | Cadastra e ajusta, no editor visual, os fluxos (matrizes curriculares) do curso ao qual está vinculado, as disciplinas, as equivalências entre fluxos e a oferta de disciplinas de cada período de matrícula |
| Admin Institucional | Cadastra os cursos, cadastra e vincula os coordenadores de cada curso e define os períodos de matrícula; também pode fazer tudo o que o coordenador faz, em qualquer curso |

## 3. Glossário

| Termo | Descrição |
|---|---|
| Curso | Curso de graduação ao qual pertencem os fluxos, com um ou mais coordenadores (nesta versão, apenas Computação) |
| Fluxo | Uma versão da matriz curricular de um curso (ex.: Fluxo 2015.1, Fluxo 2023.1), identificada pelo ano e semestre de início. É montada pelo coordenador no editor visual — do zero, duplicando outro fluxo ou importando um CSV (opcional) — e define os créditos optativos exigidos para a conclusão |
| Status do fluxo | Rascunho (visível só para a coordenação) ou Publicado. Um fluxo publicado aparece como Prévia (vigência ainda não começou), Vigente (o mais recente já em vigor) ou Em acompanhamento (substituído por um mais novo, mas ainda seguido por alunos) |
| Semestre do fluxo | Etapa numerada da matriz (1º, 2º, …) com um título (ex.: Fundamentos). Cada semestre aceita qualquer quantidade de disciplinas, exibidas lado a lado na ordem definida pelo coordenador |
| Disciplina | Possui código, nome, créditos, carga horária, semestre no fluxo, posição no semestre, pré-requisito(s), tipo (obrigatória ou optativa) e quais disciplinas ela desbloqueia |
| Catálogo de optativas (semestre 99) | Conjunto das optativas do fluxo. Optativas não pertencem a um semestre: no CSV usam o semestre 99 e, no fluxograma, aparecem em uma grade separada, sem setas |
| Pré-requisito múltiplo | Quando uma disciplina tem mais de um pré-requisito, todos precisam estar concluídos para desbloqueá-la — não basta apenas um (relação "E", não "OU") |
| Pré-requisito externo | Pré-requisito que não faz parte do fluxo (ex.: disciplina de outro curso ou de outra matriz). O coordenador o marca explicitamente como externo; a disciplina que depende dele fica Bloqueada até o aluno informar que já cumpriu esse pré-requisito |
| Cadeia de pré-requisitos | Sequência completa de disciplinas ligadas por pré-requisito, antes e depois de uma disciplina (ex.: Probabilidade e Estatística → Avaliação de Desempenho → Redes de Computadores → Sistemas Distribuídos) |
| Equivalência | Relação entre disciplinas de fluxos diferentes que representam o mesmo conteúdo (ex.: mudança apenas de nome entre o fluxo antigo e o novo); o mesmo código pode se repetir em matrizes diferentes |
| Período de Matrícula | Intervalo de datas de um semestre letivo (ex.: 2026.2), definido pelo admin institucional para cada curso, em que a oferta de disciplinas fica visível para o aluno |
| Oferta de Disciplina | Dados que variam por período de matrícula: horário (dias da semana e intervalo) ou carga flexível, para atividades sem horário fixo, e nome do professor ou responsável |
| Estado da Disciplina (por aluno) | Cursada / Cursando / Disponível (todos os pré-requisitos cursados) / Bloqueada. Cursada e Cursando são informados pelo aluno; Disponível e Bloqueada são calculados pelo sistema |
| Planejamento | Sequência de disciplinas selecionadas pelo aluno, semestre a semestre, até a formatura, salva com um nome. O aluno pode ter vários planejamentos e escolher um como principal |
| Previsão de formatura | Estimativa do semestre de conclusão e dos créditos concluídos, planejados e pendentes de um planejamento |

## 4. Histórias de Usuário

_Formato: "Como um [ator], eu quero [ação] para [benefício]", com critérios de aceitação testáveis._

### Admin Institucional

#### HU01 — Cadastrar curso

Como um **admin institucional**, eu quero **cadastrar os cursos da instituição e os seus coordenadores** para **que fluxos e ofertas fiquem organizados por curso e o sistema possa atender a mais de um curso**.

**Critérios de aceitação:**

- O sistema permite cadastrar, editar e remover cursos informando código e nome
- O sistema mantém vários cursos cadastrados, cada um com seus próprios coordenadores, fluxos e períodos de matrícula
- O admin institucional cadastra o(s) coordenador(es) de cada curso informando nome, e-mail e senha inicial; o usuário coordenador já é criado vinculado ao curso
- Cada coordenador fica vinculado a um único curso, e um curso pode ter mais de um coordenador
- O coordenador só cadastra ou altera fluxos, equivalências e ofertas do curso ao qual está vinculado; o admin institucional pode fazer essas mesmas ações em qualquer curso

#### HU02 — Definir período de matrícula

Como um **admin institucional**, eu quero **definir as datas de início e fim de cada período de matrícula** para **que horário e professor das disciplinas ofertadas apareçam para o aluno apenas durante a matrícula**.

**Critérios de aceitação:**

- O admin institucional informa o curso, o semestre letivo (ex.: 2026.2), a data de início e a data de fim do período de matrícula
- O sistema não permite, no mesmo curso, dois períodos de matrícula com datas sobrepostas nem dois períodos para o mesmo semestre letivo
- O admin institucional pode corrigir ou remover um período cadastrado
- A oferta de disciplinas (HU05) é cadastrada dentro de um período de matrícula
- Fora das datas definidas, o sistema considera que não há período de matrícula aberto (HU14)

### Coordenador

#### HU03 — Cadastrar fluxo (matriz curricular)

Como um **coordenador**, eu quero **criar um novo fluxo começando do zero, duplicando um fluxo existente ou importando um arquivo CSV** para **montar a matriz curricular sem precisar digitar tudo de novo a cada nova versão**.

**Critérios de aceitação:**

- Ao criar um fluxo, o coordenador escolhe como começar: do zero (editor em branco), duplicando um fluxo já cadastrado do mesmo curso ou importando um arquivo CSV, que é opcional
- O coordenador informa o nome do fluxo (ano e semestre de início, ex.: 2027.1), o semestre letivo de início da vigência, a quantidade inicial de semestres e os créditos optativos exigidos para a conclusão
- O fluxo fica vinculado ao curso do coordenador (HU01) e nasce como rascunho, visível apenas para a coordenação até ser publicado (HU20)
- O CSV tem uma linha por disciplina com as colunas codigo; nome; semestre (99 para optativas); creditos; carga_horaria; pre_requisitos; optativa (Sim/Não). Vários pré-requisitos são separados por vírgula e um pré-requisito externo usa o prefixo EXT: (ex.: EXT:CT904)
- O sistema aceita mais de um pré-requisito para a mesma disciplina, sendo todos obrigatórios para o desbloqueio (relação "E", não "OU")
- Antes de criar o fluxo a partir do CSV, o sistema mostra uma pré-visualização das disciplinas lidas e a quantidade de linhas válidas e com erro
- Se o arquivo tiver erro de formatação, o sistema rejeita a importação e exibe qual linha/campo está incorreto
- Se uma disciplina referenciada como pré-requisito não existir no arquivo (e não estiver marcada como externa), o sistema lista essa inconsistência com a linha e o código; o coordenador pode corrigir o CSV e enviá-lo de novo ou abrir o fluxo no editor, onde a inconsistência fica como aviso até ser resolvida (HU20)
- Após importação bem-sucedida, todas as disciplinas do CSV aparecem no editor do fluxo correspondente, com créditos, carga horária e pré-requisitos corretamente associados

#### HU04 — Cadastrar equivalência entre disciplinas de fluxos diferentes

Como um **coordenador**, eu quero **indicar que uma disciplina de um fluxo equivale a uma disciplina de outro fluxo** para **que alunos trocando de fluxo não percam o aproveitamento de disciplinas já cursadas**.

**Critérios de aceitação:**

- O sistema permite selecionar uma disciplina do Fluxo A e associá-la a uma disciplina do Fluxo B como equivalente
- O sistema não aceita equivalência entre disciplinas do mesmo fluxo nem um par já cadastrado
- Uma disciplina pode ter equivalência registrada mesmo quando difere apenas no nome (mesmo conteúdo), e o mesmo código pode se repetir em matrizes diferentes
- O coordenador vê as equivalências já cadastradas entre dois fluxos, com busca por código ou nome, e pode editar ou remover cada uma
- A equivalência cadastrada fica disponível para uso na simulação e na confirmação de troca de fluxo (HU12 e HU13)

#### HU05 — Cadastrar oferta de disciplinas do período de matrícula

Como um **coordenador**, eu quero **cadastrar horário e professor das disciplinas ofertadas em um período** para **que os alunos vejam essas informações apenas durante a matrícula daquele semestre**.

**Critérios de aceitação:**

- O coordenador seleciona um período de matrícula (HU02) e um fluxo e vê todas as disciplinas do fluxo, por semestre, e o catálogo de optativas, para marcar quais serão ofertadas
- Para cada disciplina ofertada, informa o horário (dias da semana e intervalo, ex.: SEG / QUA · 08:00–10:00) ou "carga flexível", para atividades sem horário fixo, e o nome do professor ou responsável
- Cada disciplina tem no máximo uma oferta por período (um horário e um professor); turmas paralelas da mesma disciplina não fazem parte desta versão
- O sistema só salva a oferta quando todas as disciplinas marcadas têm horário (ou carga flexível) e professor/responsável preenchidos
- Disciplinas não ofertadas naquele período não aparecem na tela de matrícula do aluno
- Horário e professor cadastrados em um período não afetam o cadastro de períodos anteriores ou futuros

#### HU20 — Ajustar o fluxograma no editor visual

Como um **coordenador**, eu quero **montar e ajustar o fluxograma do fluxo diretamente na tela, semestre a semestre** para **representar matrizes em que os semestres não têm sempre 5 ou 6 disciplinas e várias disciplinas ficam lado a lado no mesmo semestre**.

**Critérios de aceitação:**

- O editor mostra o fluxo no mesmo formato do fluxograma do aluno: uma linha por semestre e o catálogo de optativas (semestre 99) separado
- Cada semestre aceita qualquer quantidade de disciplinas, exibidas lado a lado; quando não cabem na largura da tela, a linha do semestre pode ser rolada horizontalmente
- O coordenador pode adicionar, renomear e remover semestres, e adicionar, editar, duplicar, mover e remover disciplinas
- Arrastar um card para outro semestre muda o semestre da disciplina; arrastar dentro do mesmo semestre muda a sua posição na linha, e o fluxograma do aluno respeita essa mesma ordem
- Ao selecionar uma disciplina, o coordenador edita código, nome, créditos, carga horária, semestre, tipo (obrigatória ou optativa) e pré-requisitos, inclusive externos, e vê quais disciplinas ela libera
- O sistema mostra um aviso quando uma disciplina está no mesmo semestre ou em semestre anterior ao de um pré-requisito seu, ou quando um pré-requisito vindo do CSV não foi encontrado, e oferece a correção
- O fluxo só pode ser publicado sem avisos pendentes; publicar torna o fluxo visível para os alunos, e "Ver como aluno" mostra a prévia antes disso
- As alterações do rascunho são salvas automaticamente e podem ser descartadas
- Um fluxo já publicado também pode ser editado, mas disciplinas já marcadas por algum aluno não podem ser removidas

### Aluno

#### HU06 — Selecionar fluxo inicial

Como um **aluno**, eu quero **selecionar meu fluxo (matriz curricular) no primeiro acesso** para **que o sistema saiba quais disciplinas fazem parte do meu curso**.

**Critérios de aceitação:**

- Quando há mais de um curso cadastrado, o sistema pede primeiro a escolha do curso; com um único curso (caso desta versão), mostra direto a lista de fluxos
- A lista mostra apenas fluxos publicados e já vigentes ou em acompanhamento, com a quantidade de semestres e de disciplinas obrigatórias; fluxos em prévia aparecem só na simulação de troca (HU12)
- Após a seleção, o fluxo escolhido fica associado ao aluno, que tem um único fluxo por vez
- Durante o cadastro inicial, antes de confirmar as disciplinas marcadas (HU07), o aluno pode voltar e alterar o fluxo escolhido
- Depois do cadastro inicial, o fluxo não é trocado diretamente: a troca é simulada (HU12) e só é efetivada após confirmação (HU13)

#### HU07 — Marcar disciplinas já cursadas

Como um **aluno**, eu quero **marcar quais disciplinas do meu fluxo eu já cursei** para **que o sistema calcule corretamente meu progresso e quais disciplinas estão liberadas**.

**Critérios de aceitação:**

- O sistema exibe todas as disciplinas do fluxo selecionado, agrupadas por semestre e com o catálogo de optativas, para marcação
- O sistema mostra quantas obrigatórias foram marcadas e a carga horária concluída
- Ao marcar uma disciplina como cursada, o sistema recalcula automaticamente o estado (disponível/bloqueada) das disciplinas que a têm como pré-requisito
- Quando uma disciplina tem mais de um pré-requisito, ela só passa para o estado "disponível" quando todas as disciplinas listadas como pré-requisito estiverem marcadas como cursadas
- É possível revisar e corrigir a lista de disciplinas marcadas antes de confirmar o cadastro inicial

#### HU08 — Visualizar fluxograma interativo com estado das disciplinas

Como um **aluno**, eu quero **ver um fluxograma com o estado de cada disciplina (cursada, cursando, disponível, bloqueada)** para **entender visualmente meu progresso no curso**.

**Critérios de aceitação:**

- O fluxograma é montado automaticamente a partir do fluxo cadastrado (HU03 e HU20), com uma linha por semestre, as disciplinas na ordem definida pelo coordenador e o catálogo de optativas em uma grade separada
- Cada disciplina exibida no fluxograma mostra um dos quatro estados: cursada, cursando, disponível ou bloqueada
- O fluxograma exibe uma legenda que explica como cada estado é representado e um resumo com o percentual concluído e as quantidades de cursadas, cursando e disponíveis
- Para não poluir a tela, as setas de pré-requisito aparecem só para a disciplina selecionada (HU09); sem seleção, cada card mostra quantos pré-requisitos tem (↑) e quantas disciplinas libera (↓)
- Quando aparecem, as setas não passam por cima de outras disciplinas, e é possível identificar de onde cada seta sai e para onde vai
- O esquema de cores dos estados é definido pela equipe e não usa vermelho para disciplinas a cursar
- Disciplinas optativas são visualmente identificadas de forma diferente das obrigatórias, e o card mostra o pré-requisito por escrito (semestre e código, ou "externo")
- O aluno pode buscar uma disciplina pelo nome ou pelo código
- O estado exibido reflete corretamente as disciplinas marcadas em HU07, HU18 e HU19

#### HU09 — Ver detalhes e cadeia de pré-requisitos de uma disciplina

Como um **aluno**, eu quero **clicar em uma disciplina do fluxograma e ver seus detalhes e sua cadeia de pré-requisitos** para **entender o que vem antes dela, o que ela desbloqueia e o caminho até o fim do curso antes de planejar cursá-la**.

**Critérios de aceitação:**

- Ao clicar em uma disciplina, o sistema exibe: código, nome, estado, tipo (obrigatória ou optativa), créditos, carga horária, semestre, pré-requisito(s) com o seu estado e quais disciplinas ela desbloqueia
- Ao clicar em uma disciplina, o fluxograma entra em modo foco e destaca a cadeia completa de pré-requisitos: as disciplinas que vêm antes dela e as que dependem dela, direta ou indiretamente (ex.: Probabilidade e Estatística → Avaliação de Desempenho → Redes de Computadores → Sistemas Distribuídos), com setas diferentes para pré-requisito direto, indireto e para o que ela libera
- Enquanto a cadeia está destacada, as demais disciplinas ficam esmaecidas; o destaque some ao clicar fora ou em "Sair do foco"
- No painel de detalhes, o aluno pode marcar ou desmarcar a disciplina como cursada ou cursando (HU18 e HU19) e informar que já cumpriu um pré-requisito externo
- A informação exibida corresponde exatamente ao cadastro feito pelo coordenador (HU03 e HU20)

#### HU10 — Simular planejamento de matrícula semestre a semestre

Como um **aluno**, eu quero **selecionar disciplinas disponíveis e ver quais novas disciplinas seriam desbloqueadas, avançando semestre a semestre** para **montar um planejamento completo de matrícula até a formatura**.

**Critérios de aceitação:**

- O sistema lista, para o semestre simulado, todas as disciplinas com estado "disponível" (desbloqueadas e não cursadas), inclusive optativas, e permite filtrar pelo semestre do fluxo
- O aluno pode selecionar mais de uma disciplina disponível para o mesmo semestre simulado, e o sistema mostra o total de créditos selecionados
- Ao selecionar uma disciplina, o sistema recalcula e exibe imediatamente (sem precisar recarregar a página) quais disciplinas passariam a ter estado "disponível" caso as selecionadas até aquele momento sejam concluídas
- Uma disciplina com mais de um pré-requisito só é exibida como desbloqueada na simulação quando todos os seus pré-requisitos estiverem marcados como cursados ou selecionados em semestres anteriores da simulação
- Disciplinas marcadas como cursando (HU18) não aparecem como selecionáveis e contam como concluídas para o desbloqueio a partir do primeiro semestre simulado
- O sistema permite avançar para um novo semestre simulado após a seleção do semestre atual, considerando as disciplinas de semestres anteriores da simulação como "concluídas" para efeito de desbloqueio
- O aluno pode voltar a um semestre simulado anterior e alterar a seleção antes de finalizar o planejamento
- A seleção feita durante a simulação não representa uma matrícula real no sistema

#### HU11 — Salvar planejamento

Como um **aluno**, eu quero **salvar o planejamento de semestres que montei** para **poder consultá-lo ou retomá-lo depois sem refazer a simulação do zero**.

**Critérios de aceitação:**

- O sistema salva, com um nome, a sequência completa de disciplinas por semestre, associada ao aluno e ao fluxo em que o planejamento foi montado
- O aluno pode ter vários planejamentos salvos, com nomes diferentes, e marcar um deles como principal, usado por padrão na previsão de formatura (HU21) e na matrícula do período (HU14)
- Após salvar, o aluno recebe confirmação de que o planejamento foi armazenado
- A lista de planejamentos mostra, para cada um, a conclusão prevista, os créditos planejados em relação aos exigidos e as datas de criação e de última edição; quando não há nenhum, mostra como começar
- O aluno consegue abrir, alterar, renomear ou remover um planejamento salvo em uma sessão futura

#### HU12 — Simular troca de fluxo

Como um **aluno**, eu quero **simular como ficaria meu progresso em outro fluxo (matriz curricular)** para **decidir se vale a pena migrar antes de fazer a troca de verdade**.

**Critérios de aceitação:**

- O aluno escolhe um fluxo publicado do mesmo curso, diferente do seu, inclusive fluxos em prévia
- O sistema aplica as equivalências cadastradas (HU04) entre o fluxo atual do aluno e o fluxo simulado
- O sistema exibe quantas e quais disciplinas seriam aproveitadas por equivalência e quantas e quais ainda faltariam no fluxo simulado
- A partir da simulação de troca, o aluno pode iniciar um planejamento (HU10) já no fluxo simulado, sem que isso altere seu fluxo real até a confirmação (HU13)

#### HU13 — Confirmar troca de fluxo

Como um **aluno**, eu quero **confirmar a troca para o fluxo que simulei** para **que meu progresso passe a ser acompanhado no novo fluxo**.

**Critérios de aceitação:**

- A troca só é efetivada depois que o aluno confirma, na tela de simulação de troca (HU12)
- Antes de confirmar, o sistema mostra quais disciplinas cursadas não têm equivalência no novo fluxo, ou informa que todas têm
- Após a confirmação, o aluno fica associado apenas ao novo fluxo, e as disciplinas cursadas com equivalência (HU04) aparecem como cursadas no novo fluxo
- A troca altera apenas o fluxo usado no sistema; ela não altera o vínculo oficial do aluno na instituição

#### HU14 — Visualizar oferta de disciplinas do período de matrícula

Como um **aluno**, eu quero **ver horário e professor das disciplinas ofertadas no período de matrícula** para **decidir quais cursar considerando minha disponibilidade de horário**.

**Critérios de aceitação:**

- Durante o período de matrícula (HU02), o aluno vê as disciplinas ofertadas que estão disponíveis para ele, separadas em obrigatórias e optativas, com horário (ou carga flexível), nome do professor e créditos cadastrados pelo coordenador (HU05)
- A seleção do período parte de um planejamento salvo (o principal, por padrão) e corresponde ao semestre desse planejamento que cai no período de matrícula; o aluno pode trocar o planejamento usado
- Disciplinas não ofertadas naquele período específico não aparecem com essas informações
- Fora do período de matrícula, o sistema informa que não há período aberto e oculta horário/professor, mostrando apenas nome, código e pré-requisitos

#### HU15 — Selecionar disciplinas optativas dentre as ofertadas

Como um **aluno**, eu quero **escolher livremente quais optativas cursar dentre as disponíveis** para **montar meu planejamento de acordo com meu interesse, mesmo quando há mais opções do que preciso cursar**.

**Critérios de aceitação:**

- O sistema identifica visualmente quais disciplinas disponíveis são optativas
- O aluno pode selecionar qualquer subconjunto das optativas disponíveis ou ofertadas (ex.: 3 de 20) para incluir no planejamento
- O sistema mostra quantos créditos optativos o aluno já cursou ou planejou e quantos ainda faltam, conforme os créditos optativos exigidos pelo fluxo (HU03)
- A seleção de uma optativa segue as mesmas regras de desbloqueio dinâmico das demais disciplinas descritas em HU10

#### HU16 — Detectar choque de horário entre disciplinas selecionadas

Como um **aluno**, eu quero **que o sistema me avise quando duas disciplinas que selecionei no período de matrícula têm horário conflitante** para **eu poder escolher outra disciplina antes de confirmar**.

**Critérios de aceitação:**

- Ao selecionar uma disciplina com horário cadastrado (HU05) que conflita com outra já selecionada no mesmo período, o sistema sinaliza o conflito antes da confirmação
- O aviso indica quais disciplinas estão em conflito e em qual dia e horário
- Ofertas com carga flexível não geram conflito de horário
- O aluno pode salvar a seleção como rascunho mesmo com conflito, mas só consegue confirmá-la depois de resolvê-lo; confirmar salva o semestre no planejamento e não realiza matrícula oficial
- O aluno consegue trocar a seleção sem perder o restante do planejamento já feito naquele período

#### HU17 — Informar horários indisponíveis da rotina pessoal

Como um **aluno**, eu quero **informar os horários em que não posso ter aula** para **que o sistema sinalize as disciplinas conflitantes com esse horário durante a matrícula**.

**Critérios de aceitação:**

- O aluno consegue cadastrar blocos de horário indisponíveis, no mesmo formato de dia da semana e intervalo usado na oferta (HU05)
- Disciplinas ofertadas em horário que conflita com um bloco indisponível são sinalizadas do mesmo jeito que o conflito entre disciplinas (HU16)
- O aluno pode editar ou remover os horários indisponíveis cadastrados a qualquer momento

#### HU18 — Marcar disciplinas cursando no semestre atual

Como um **aluno**, eu quero **marcar quais disciplinas estou cursando nesse semestre** para **que o fluxograma reflita meu progresso real, e não apenas o que já concluí ou que estou simulando para o futuro**.

**Critérios de aceitação:**

- O aluno pode marcar uma ou mais disciplinas disponíveis como “Cursando”, pelo painel de detalhes da disciplina (HU09)
- Uma disciplina marcada como “Cursando” aparece no fluxograma (HU08) com o estado correspondente, diferente de “Cursada” e “Disponível”
- No início do próximo semestre, o aluno pode confirmar cada disciplina marcada como “Cursando” como concluída (Cursada) ou não concluída (Disponível)

#### HU19 — Corrigir disciplina marcada após cadastro inicial

Como um **aluno**, eu quero **poder desmarcar uma disciplina que eu tinha marcado como cursada ou cursando — por exemplo, quando tranquei ou reprovei a disciplina** para **que meu fluxograma continue correto**.

**Critérios de aceitação:**

- O aluno pode alterar o estado de qualquer disciplina marcada anteriormente, mesmo depois do cadastro inicial confirmado (HU07), pelo painel de detalhes da disciplina (HU09)
- Ao desmarcar uma disciplina como cursada, o sistema recalcula o estado de todas as disciplinas que dependiam dela como pré-requisito
- Uma disciplina trancada ou reprovada volta ao estado “Disponível”, nunca “Cursada”, sem exigir marcação visual diferente

#### HU21 — Visualizar previsão de formatura

Como um **aluno**, eu quero **ver a previsão de formatura de um planejamento** para **saber quando devo concluir o curso e o que ainda falta incluir no plano**.

**Critérios de aceitação:**

- O sistema mostra o semestre letivo de conclusão prevista (o último semestre do planejamento) e a duração do percurso em semestres, deixando claro que é uma estimativa
- O sistema mostra, semestre a semestre, a quantidade de disciplinas e de créditos concluídos e planejados
- O sistema mostra os créditos concluídos, planejados e pendentes em relação ao total exigido, que é a soma dos créditos das obrigatórias com os créditos optativos exigidos pelo fluxo (HU03)
- Quando o planejamento não cobre todo o exigido, o sistema avisa o que falta (ex.: créditos optativos) e leva às disciplinas pendentes
- A previsão usa o planejamento principal por padrão e pode ser vista para qualquer planejamento salvo (HU11)

### Todos os atores

#### HU22 — Criar conta e acessar o sistema

Como um **usuário (aluno, coordenador ou admin institucional)**, eu quero **entrar no sistema com as minhas credenciais** para **acessar as funcionalidades do meu papel**.

**Critérios de aceitação:**

- O aluno cria a própria conta informando matrícula, nome, e-mail e senha (mínimo de 8 caracteres); o papel Aluno é atribuído automaticamente
- Coordenadores são cadastrados pelo admin institucional (HU01), e o primeiro admin institucional é criado na instalação do sistema
- Matrícula e e-mail não podem estar em uso por outra conta
- O login aceita e-mail ou matrícula (no caso do aluno) e senha; credenciais inválidas exibem uma mensagem de erro sem indicar qual dos dois campos está incorreto
- Após o login, cada papel vai para a sua área: aluno sem fluxo vai para a seleção de fluxo (HU06), os demais alunos vão para o fluxograma (HU08), e coordenador e admin institucional vão para o painel de gestão
