import { NOME_ESTADO } from '../rotulos'
import type { Disciplina } from '../tipos'

export type Destaque = 'selecionada' | 'pre' | 'pre-indireto' | 'libera' | 'libera-indireto'

const NOME_DESTAQUE: Record<Exclude<Destaque, 'selecionada'>, string> = {
  pre: 'Pré-requisito',
  'pre-indireto': 'Pré-req. indireto',
  libera: 'Libera',
  'libera-indireto': 'Libera depois',
}

export function rotuloEstado(d: Disciplina) {
  const estado = d.estado ?? 'BLOQUEADA'
  if (d.tipo === 'EXTERNA') return estado === 'CURSADA' ? 'Cumprido' : 'Pendente'
  return NOME_ESTADO[estado]
}

export function CartaoDisciplina({
  disciplina: d,
  destaque,
  esmaecida,
  aoClicar,
}: {
  disciplina: Disciplina
  destaque?: Destaque
  esmaecida: boolean
  aoClicar: () => void
}) {
  const estado = (d.estado ?? 'BLOQUEADA').toLowerCase()
  const classes = [
    'cartao',
    `estado-${estado}`,
    `tipo-${d.tipo.toLowerCase()}`,
    destaque ? `destaque-${destaque}` : '',
    esmaecida ? 'esmaecida' : '',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <button
      type="button"
      className={classes}
      onClick={(e) => {
        e.stopPropagation()
        aoClicar()
      }}
      aria-pressed={destaque === 'selecionada'}
      title={`${d.codigo} — ${d.nome}`}
    >
      <span className="cartao-estado">
        <span className="ponto" aria-hidden />
        {rotuloEstado(d)}
      </span>
      <span className="cartao-codigo">{d.codigo}</span>
      <span className="cartao-nome">{d.nome}</span>
      <span className="cartao-rodape">
        <span>{d.preRequisitos.length ? `${d.preRequisitos.length} pré` : 'sem pré'}</span>
        <span>{d.creditos} cr</span>
        {d.libera.length > 0 && <span>libera {d.libera.length}</span>}
      </span>
      {destaque && destaque !== 'selecionada' && <span className="cartao-marca">{NOME_DESTAQUE[destaque]}</span>}
    </button>
  )
}
