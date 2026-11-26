import { useState } from 'react'
import { ErroApi } from '../api'
import { NOME_ESTADO, NOME_TIPO, ordinalSemestre } from '../rotulos'
import type { Disciplina, DisciplinaDetalhe, ItemCadeia, Situacao } from '../tipos'
import { rotuloEstado } from './CartaoDisciplina'

interface Acao {
  rotulo: string
  estilo: 'primario' | 'secundario' | 'discreto'
  executar: () => Promise<unknown>
  sucesso: string
}

/** Painel de detalhes (HU09) com as ações de HU07, HU18 e HU19. */
export function PainelDisciplina({
  disciplina: d,
  detalhe,
  aoFechar,
  aoAbrir,
  aoMarcar,
  aoDesmarcar,
}: {
  disciplina: Disciplina
  detalhe: DisciplinaDetalhe | null
  aoFechar: () => void
  aoAbrir: (id: number) => void
  aoMarcar: (situacao: Situacao) => Promise<unknown>
  aoDesmarcar: () => Promise<unknown>
}) {
  const [executando, setExecutando] = useState(false)
  const [mensagem, setMensagem] = useState<{ tipo: 'ok' | 'erro'; texto: string; itens?: string[] } | null>(null)
  const estado = d.estado ?? 'BLOQUEADA'

  const preDiretos = detalhe?.cadeiaAntes.filter((i) => i.nivel === 1) ?? []
  const preIndiretos = detalhe?.cadeiaAntes.filter((i) => i.nivel > 1) ?? []
  const liberaDiretas = detalhe?.cadeiaDepois.filter((i) => i.nivel === 1) ?? []
  const liberaDepois = detalhe?.cadeiaDepois.filter((i) => i.nivel > 1) ?? []
  const faltando = preDiretos.filter((i) => i.estado !== 'CURSADA')

  const marcar = (situacao: Situacao): Acao['executar'] => () => aoMarcar(situacao)
  const acoes: Acao[] = []
  const desmarcar: Acao = {
    rotulo: d.tipo === 'EXTERNA' ? 'Ainda não cumpri' : 'Desmarcar',
    estilo: 'discreto',
    executar: aoDesmarcar,
    sucesso: `${d.codigo} desmarcada.`,
  }
  if (d.tipo === 'EXTERNA') {
    if (estado === 'CURSADA') acoes.push(desmarcar)
    else
      acoes.push({
        rotulo: `Já cumpri ${d.codigo}`,
        estilo: 'primario',
        executar: marcar('CURSADA'),
        sucesso: `${d.codigo} marcado como cumprido.`,
      })
  } else if (estado === 'CURSADA') {
    acoes.push(desmarcar)
  } else if (estado === 'CURSANDO') {
    acoes.push(
      { rotulo: 'Marcar como cursada', estilo: 'primario', executar: marcar('CURSADA'), sucesso: `${d.codigo} marcada como cursada.` },
      desmarcar,
    )
  } else {
    acoes.push({
      rotulo: 'Marcar como cursada',
      estilo: estado === 'DISPONIVEL' ? 'primario' : 'secundario',
      executar: marcar('CURSADA'),
      sucesso: `${d.codigo} marcada como cursada.`,
    })
    if (estado === 'DISPONIVEL')
      acoes.push({
        rotulo: 'Marcar como cursando',
        estilo: 'secundario',
        executar: marcar('CURSANDO'),
        sucesso: `${d.codigo} marcada como cursando.`,
      })
  }

  async function executar(acao: Acao) {
    setExecutando(true)
    setMensagem(null)
    try {
      await acao.executar()
      setMensagem({ tipo: 'ok', texto: acao.sucesso })
    } catch (e) {
      if (e instanceof ErroApi)
        setMensagem({
          tipo: 'erro',
          texto: e.message,
          itens: e.detalhes.map((x) => x.mensagem).filter((x): x is string => Boolean(x)),
        })
      else setMensagem({ tipo: 'erro', texto: 'Não foi possível concluir a ação' })
    } finally {
      setExecutando(false)
    }
  }

  const ondeFica =
    d.semestre != null ? ordinalSemestre(d.semestre) : d.tipo === 'OPTATIVA' ? 'Catálogo de optativas' : 'Fora do fluxo'

  return (
    <aside className="painel" aria-label={`Detalhes de ${d.codigo}`}>
      <div className="painel-topo">
        <p className="sobretitulo">Detalhes da disciplina</p>
        <button type="button" className="fechar" aria-label="Fechar e sair do foco" onClick={aoFechar}>
          ×
        </button>
      </div>

      <p className="painel-codigo">{d.codigo}</p>
      <h2>{d.nome}</h2>
      <div className="selos">
        <span className={`selo estado-${estado.toLowerCase()}`}>{rotuloEstado(d)}</span>
        <span className="selo neutro">{NOME_TIPO[d.tipo]}</span>
      </div>

      <dl className="ficha">
        <div>
          <dt>Créditos</dt>
          <dd>{d.creditos}</dd>
        </div>
        <div>
          <dt>Carga horária</dt>
          <dd>{d.cargaHoraria} h</dd>
        </div>
        <div>
          <dt>Onde fica</dt>
          <dd>{ondeFica}</dd>
        </div>
      </dl>

      <div className="painel-acoes">
        {acoes.map((a) => (
          <button
            key={a.rotulo}
            type="button"
            className={`botao ${a.estilo}`}
            disabled={executando}
            onClick={() => void executar(a)}
          >
            {a.rotulo}
          </button>
        ))}
      </div>
      {estado === 'BLOQUEADA' && d.tipo !== 'EXTERNA' && faltando.length > 0 && (
        <p className="nota">
          Fica disponível quando você concluir {faltando.map((i) => i.codigo).join(', ')}. Se já cursou esta disciplina,
          pode marcá-la como cursada mesmo assim.
        </p>
      )}
      {estado === 'CURSANDO' && (
        <p className="nota">
          No início do próximo semestre, confirme: concluiu → "Marcar como cursada"; trancou ou reprovou → "Desmarcar"
          (volta a Disponível).
        </p>
      )}
      {estado === 'CURSADA' && d.tipo !== 'EXTERNA' && (
        <p className="nota">Trancou, reprovou ou marcou por engano? "Desmarcar" recalcula o que dependia dela.</p>
      )}
      {mensagem && (
        <div className={`alerta ${mensagem.tipo === 'ok' ? 'sucesso' : 'erro'}`} role="status">
          {mensagem.texto}
          {mensagem.itens && mensagem.itens.length > 0 && (
            <ul>
              {mensagem.itens.map((i) => (
                <li key={i}>{i}</li>
              ))}
            </ul>
          )}
        </div>
      )}

      {!detalhe ? (
        <p className="carregando">Carregando a cadeia de pré-requisitos…</p>
      ) : (
        <>
          <ListaCadeia titulo="Pré-requisitos" vazio="Sem pré-requisitos." itens={preDiretos} aoAbrir={aoAbrir} />
          {preIndiretos.length > 0 && (
            <ListaCadeia titulo="Antes deles (indiretos)" itens={preIndiretos} aoAbrir={aoAbrir} discreta />
          )}
          <ListaCadeia
            titulo="Desbloqueia"
            vazio="Nenhuma disciplina depende desta."
            itens={liberaDiretas}
            aoAbrir={aoAbrir}
          />
          {liberaDepois.length > 0 && (
            <ListaCadeia titulo="Mais adiante (indiretas)" itens={liberaDepois} aoAbrir={aoAbrir} discreta />
          )}
          <p className="painel-rodape">Fluxo {detalhe.fluxoNome} · informação cadastrada pela coordenação</p>
        </>
      )}
    </aside>
  )
}

function ListaCadeia({
  titulo,
  itens,
  vazio,
  aoAbrir,
  discreta = false,
}: {
  titulo: string
  itens: ItemCadeia[]
  vazio?: string
  aoAbrir: (id: number) => void
  discreta?: boolean
}) {
  if (!itens.length && !vazio) return null
  return (
    <section className={`cadeia ${discreta ? 'discreta' : ''}`}>
      <h3>{titulo}</h3>
      {itens.length === 0 ? (
        <p className="texto-apoio">{vazio}</p>
      ) : (
        <ul>
          {itens.map((i) => (
            <li key={i.id}>
              <button type="button" onClick={() => aoAbrir(i.id)}>
                <span className="codigo">{i.codigo}</span>
                <span className="nome">{i.nome}</span>
                {i.estado && (
                  <span className={`selo mini estado-${i.estado.toLowerCase()}`}>
                    {i.tipo === 'EXTERNA' ? (i.estado === 'CURSADA' ? 'Cumprido' : 'Pendente') : NOME_ESTADO[i.estado]}
                  </span>
                )}
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
