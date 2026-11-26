import type { EstadoDisciplina, RotuloFluxo, TipoDisciplina } from './tipos'

export const NOME_ESTADO: Record<EstadoDisciplina, string> = {
  CURSADA: 'Cursada',
  CURSANDO: 'Cursando',
  DISPONIVEL: 'Disponível',
  BLOQUEADA: 'Bloqueada',
}

export const NOME_ROTULO: Record<RotuloFluxo, string> = {
  RASCUNHO: 'Rascunho',
  PREVIA: 'Prévia',
  VIGENTE: 'Vigente',
  EM_ACOMPANHAMENTO: 'Em acompanhamento',
}

export const NOME_TIPO: Record<TipoDisciplina, string> = {
  OBRIGATORIA: 'Obrigatória',
  OPTATIVA: 'Optativa',
  EXTERNA: 'Pré-requisito externo',
}

export function ordinalSemestre(numero: number) {
  return `${numero}º semestre`
}
