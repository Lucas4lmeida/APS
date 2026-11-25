# Protótipo no Figma

Arquivo: [Fluxograma Acadêmico](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico) — base visual do front-end. Os textos e números inconsistentes já foram corrigidos no próprio arquivo (itens C1–C8 e B-✅ de [revisao-de-discrepancias.md](revisao-de-discrepancias.md)); o histórico de versões do Figma mostra cada alteração.

Os dados das telas são de exemplo (aluna Ana Costa, fluxo 2023.1 de Ciência da Computação, período 2026.2).

## Telas existentes × HUs

| Seção | Tela | HUs |
|---|---|---|
| 01 · Acesso e configuração do aluno | [Login web](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=3-125) · [Login mobile](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=3-168) | HU22 (falta a tela de cadastro) |
| | [Selecionar fluxo](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=4-297) | HU06 |
| | [Marcar disciplinas cursadas](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=4-349) | HU07 |
| 02 · Jornada do aluno | [Matriz curricular](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=6-979) | HU08, HU15 |
| | [Detalhes de disciplina](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=6-4650) · [Matriz curricular — modo foco](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=84-2109) | HU09 (faltam as ações de HU18/HU19) |
| | [Simulação de matrícula](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-1618) | HU10, HU15 |
| | [Meus planejamentos](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-1798) · [estado vazio](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-2030) | HU11 |
| | [Previsão de formatura](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-1896) | HU21 |
| | [Simulação de troca de fluxo](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-3113) | HU12 (falta a confirmação da HU13) |
| | [Matrícula do período](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-3369) | HU14, HU15, HU16 |
| 03 · Coordenador | [Painel do coordenador](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-6114) | HU03, HU04, HU05 |
| | [Novo fluxo — como começar](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=84-804) | HU03 |
| | [Cadastrar fluxo — importar CSV (opcional)](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-6216) | HU03 |
| | [Editor de fluxo](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=84-2) | HU20 |
| | [Cadastrar equivalência](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-6348) | HU04 |
| | [Cadastrar oferta do período](https://www.figma.com/design/9nVlzI8Q4iPhECKAuVFDvz/Fluxograma-Acad%C3%AAmico?node-id=10-6509) | HU05 |

**HUs sem nenhuma tela:** HU01, HU02, HU17, HU18, HU19.

## Telas que faltam

Desenhar antes de implementar a tela correspondente. O comportamento já está definido em [historias-de-usuario.md](historias-de-usuario.md) e [arquitetura.md](arquitetura.md).

| # | Tela | HU | O que precisa ter |
|---|---|---|---|
| D1 | Criar conta (aluno) | HU22 | Matrícula, nome, e-mail, senha e confirmação; erro de matrícula ou e-mail já em uso; link de volta ao login (o login já tem "Primeiro acesso? Criar conta") |
| D2 | Admin: cursos e coordenadores | HU01 | Lista de cursos (código, nome) com criar/editar/remover; por curso, coordenadores vinculados e cadastro de coordenador (nome, e-mail, senha inicial) |
| D3 | Admin: períodos de matrícula | HU02 | Por curso: semestre letivo, data de início e de fim; editar/remover; erro de período sobreposto ou semestre repetido |
| D4 | Ações no painel de detalhes | HU09, HU18, HU19 | Botões "Marcar como cursada", "Marcar como cursando", "Desmarcar"; em disciplina com pré-requisito externo, "Já cumpri CT904"; confirmação de "Cursando → Cursada/Disponível" no início do semestre |
| D5 | Horários indisponíveis | HU17 | Cadastro de blocos (dia + intervalo) com editar/remover; na Matrícula do período, o aviso de conflito com horário indisponível no mesmo estilo do conflito entre disciplinas |
| D6 | Confirmar troca de fluxo | HU13 | Botão "Confirmar troca" na Simulação de troca e diálogo listando as cursadas sem equivalência (ou "todas têm equivalência") |
| D7 | Matrícula do período: fora do período e escolha do plano | HU14 | Estado "nenhum período aberto" (sem horário/professor, só nome, código e pré-requisitos) e seletor do planejamento usado (o principal por padrão) |
| D8 | Novo fluxo: créditos optativos | HU03 | Campo "Créditos optativos exigidos" na identificação do fluxo |
| D9 | Meus planejamentos: ações | HU11 | "Definir como principal", renomear e remover em cada card |
| D10 | Navegação do aluno | — | Uma navegação única com Matriz, Simulação, Matrícula do período, Troca de fluxo, Meus planejamentos e Previsão de formatura |
| D11 | Painel do coordenador | HU03, HU20 | Listar também o fluxo 2027.1 com o rótulo Rascunho/Prévia |
| D12 | Importar CSV: pré-visualização | HU03 | Coluna "Carga horária" na tabela de pré-visualização |
