import type {
  Curso,
  DetalheErro,
  DisciplinaDetalhe,
  Fluxograma,
  FluxoResumo,
  Sessao,
  Situacao,
} from './tipos'

const BASE = '/api'
const CHAVE_SESSAO = 'aps.sessao'

/** Erro no formato único da API: { "erro": "...", "detalhes": [...] } (arquitetura, seção 4.6). */
export class ErroApi extends Error {
  readonly status: number
  readonly detalhes: DetalheErro[]

  constructor(status: number, mensagem: string, detalhes: DetalheErro[] = []) {
    super(mensagem)
    this.status = status
    this.detalhes = detalhes
  }

  /** Mensagem do primeiro detalhe ligado ao campo, se houver. */
  doCampo(campo: string): string | undefined {
    return this.detalhes.find((d) => d.campo === campo)?.mensagem
  }
}

export function lerSessao(): Sessao | null {
  const texto = localStorage.getItem(CHAVE_SESSAO)
  if (!texto) return null
  try {
    const sessao = JSON.parse(texto) as Sessao
    if (new Date(sessao.expiraEm).getTime() <= Date.now()) {
      localStorage.removeItem(CHAVE_SESSAO)
      return null
    }
    return sessao
  } catch {
    localStorage.removeItem(CHAVE_SESSAO)
    return null
  }
}

export function salvarSessao(sessao: Sessao | null) {
  if (sessao) localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao))
  else localStorage.removeItem(CHAVE_SESSAO)
}

let aoExpirar: () => void = () => {}

/** Chamado quando a API responde 401 (token vencido ou inválido). */
export function definirAoExpirar(callback: () => void) {
  aoExpirar = callback
}

async function requisicao<T>(metodo: string, caminho: string, corpo?: unknown): Promise<T> {
  const cabecalhos: Record<string, string> = { Accept: 'application/json' }
  if (corpo !== undefined) cabecalhos['Content-Type'] = 'application/json'
  const sessao = lerSessao()
  if (sessao) cabecalhos.Authorization = `Bearer ${sessao.token}`

  let resposta: Response
  try {
    resposta = await fetch(BASE + caminho, {
      method: metodo,
      headers: cabecalhos,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    })
  } catch {
    throw new ErroApi(0, 'Não foi possível falar com o servidor. Confira se o back-end está rodando.')
  }

  if (resposta.status === 204) return undefined as T
  const texto = await resposta.text()
  const dados = texto ? safeJson(texto) : undefined

  if (!resposta.ok) {
    const erro = dados as { erro?: string; detalhes?: DetalheErro[] } | undefined
    if (resposta.status === 401 && sessao) aoExpirar()
    throw new ErroApi(
      resposta.status,
      erro?.erro ?? `Erro ${resposta.status} ao chamar a API`,
      erro?.detalhes ?? [],
    )
  }
  return dados as T
}

function safeJson(texto: string): unknown {
  try {
    return JSON.parse(texto)
  } catch {
    return undefined
  }
}

export const api = {
  login: (login: string, senha: string) => requisicao<Sessao>('POST', '/auth/login', { login, senha }),
  registrar: (dados: { matricula: string; nome: string; email: string; senha: string }) =>
    requisicao<Sessao>('POST', '/auth/registro', dados),
  cursos: () => requisicao<Curso[]>('GET', '/cursos'),
  fluxos: (cursoId: number) => requisicao<FluxoResumo[]>('GET', `/cursos/${cursoId}/fluxos`),
  selecionarFluxo: (alunoId: number, fluxoId: number) =>
    requisicao<{ id: number; fluxoId: number }>('PUT', `/alunos/${alunoId}/fluxo`, { fluxoId }),
  fluxograma: (alunoId: number) => requisicao<Fluxograma>('GET', `/alunos/${alunoId}/fluxograma`),
  disciplina: (disciplinaId: number) => requisicao<DisciplinaDetalhe>('GET', `/disciplinas/${disciplinaId}`),
  marcar: (alunoId: number, disciplinaId: number, situacao: Situacao) =>
    requisicao<unknown>('PUT', `/alunos/${alunoId}/disciplinas/${disciplinaId}`, { situacao }),
  desmarcar: (alunoId: number, disciplinaId: number) =>
    requisicao<void>('DELETE', `/alunos/${alunoId}/disciplinas/${disciplinaId}`),
}
