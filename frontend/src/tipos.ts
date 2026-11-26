// Formatos da API (backend/src/main/java/br/aps/fluxograma/dto e docs/arquitetura.md, seção 4).

export type Papel = 'ALUNO' | 'COORDENADOR' | 'ADMIN_INSTITUCIONAL'
export type EstadoDisciplina = 'CURSADA' | 'CURSANDO' | 'DISPONIVEL' | 'BLOQUEADA'
export type Situacao = 'CURSADA' | 'CURSANDO'
export type TipoDisciplina = 'OBRIGATORIA' | 'OPTATIVA' | 'EXTERNA'
export type RotuloFluxo = 'RASCUNHO' | 'PREVIA' | 'VIGENTE' | 'EM_ACOMPANHAMENTO'

export interface Usuario {
  id: number
  nome: string
  email: string
  papel: Papel
  matricula?: string
  fluxoId?: number
  cursoId?: number
}

export interface Sessao {
  token: string
  tipo: string
  expiraEm: string
  usuario: Usuario
}

export interface Curso {
  id: number
  codigo: string
  nome: string
}

export interface FluxoResumo {
  id: number
  cursoId: number
  nome: string
  vigenciaInicio: string
  status: 'RASCUNHO' | 'PUBLICADO'
  rotulo: RotuloFluxo
  quantidadeSemestres: number
  quantidadeObrigatorias: number
  quantidadeOptativas: number
  creditosObrigatorios: number
  creditosOptativosExigidos: number
  totalCreditosExigidos: number
}

export interface Disciplina {
  id: number
  codigo: string
  nome: string
  creditos: number
  cargaHoraria: number
  tipo: TipoDisciplina
  semestre: number | null
  ordem: number
  pendente: boolean
  preRequisitos: number[]
  libera: number[]
  estado?: EstadoDisciplina
}

export interface Semestre {
  numero: number
  titulo: string | null
  disciplinas: Disciplina[]
}

export interface FluxoDetalhe {
  id: number
  curso: Curso
  nome: string
  vigenciaInicio: string
  status: 'RASCUNHO' | 'PUBLICADO'
  rotulo: RotuloFluxo
  creditosObrigatorios: number
  creditosOptativosExigidos: number
  totalCreditosExigidos: number
  semestres: Semestre[]
  optativas: Disciplina[]
  externas: Disciplina[]
}

export interface ResumoProgresso {
  percentualConcluido: number
  cursadas: number
  cursando: number
  disponiveis: number
  bloqueadas: number
  obrigatoriasTotal: number
  obrigatoriasCursadas: number
  creditosConcluidos: number
  totalCreditosExigidos: number
  cargaHorariaConcluida: number
  creditosOptativosCursados: number
  creditosOptativosCursando: number
  creditosOptativosExigidos: number
  creditosOptativosFaltantes: number
}

export interface Fluxograma {
  aluno: { id: number; nome: string; matricula: string; fluxoId: number | null }
  fluxo: FluxoDetalhe
  resumo: ResumoProgresso
}

export interface ItemCadeia {
  id: number
  codigo: string
  nome: string
  tipo: TipoDisciplina
  semestre?: number
  nivel: number
  estado?: EstadoDisciplina
}

export interface DisciplinaDetalhe {
  disciplina: Disciplina
  fluxoId: number
  fluxoNome: string
  cadeiaAntes: ItemCadeia[]
  cadeiaDepois: ItemCadeia[]
}

export interface DetalheErro {
  linha?: number
  campo?: string
  mensagem?: string
}
