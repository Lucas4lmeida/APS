import { useCallback, useEffect, useMemo, useState } from 'react'
import { api, ErroApi } from '../api'
import { CartaoDisciplina, type Destaque } from '../componentes/CartaoDisciplina'
import { PainelDisciplina } from '../componentes/PainelDisciplina'
import { NOME_ROTULO, ordinalSemestre } from '../rotulos'
import type { Disciplina, DisciplinaDetalhe, Fluxograma, ResumoProgresso } from '../tipos'

/** HU07, HU08, HU09, HU18 e HU19: matriz com estados, resumo, modo foco e marcações. */
export function Matriz({ alunoId, aoAlterarFluxo }: { alunoId: number; aoAlterarFluxo: () => void }) {
  const [dados, setDados] = useState<Fluxograma | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [selecionadaId, setSelecionadaId] = useState<number | null>(null)
  const [detalhe, setDetalhe] = useState<DisciplinaDetalhe | null>(null)
  const [busca, setBusca] = useState('')
  const [marcacaoRapida, setMarcacaoRapida] = useState(false)
  const [avisoRapido, setAvisoRapido] = useState<string | null>(null)

  const carregar = useCallback(async () => {
    try {
      setDados(await api.fluxograma(alunoId))
      setErro(null)
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Erro ao carregar o fluxograma')
    }
  }, [alunoId])

  const carregarDetalhe = useCallback(async (id: number) => {
    try {
      setDetalhe(await api.disciplina(id))
    } catch {
      setDetalhe(null)
    }
  }, [])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const fechar = useCallback(() => {
    setSelecionadaId(null)
    setDetalhe(null)
  }, [])

  useEffect(() => {
    const aoTeclar = (e: KeyboardEvent) => e.key === 'Escape' && fechar()
    window.addEventListener('keydown', aoTeclar)
    return () => window.removeEventListener('keydown', aoTeclar)
  }, [fechar])

  const porId = useMemo(() => {
    const mapa = new Map<number, Disciplina>()
    if (!dados) return mapa
    for (const s of dados.fluxo.semestres) for (const d of s.disciplinas) mapa.set(d.id, d)
    for (const d of dados.fluxo.optativas) mapa.set(d.id, d)
    for (const d of dados.fluxo.externas) mapa.set(d.id, d)
    return mapa
  }, [dados])

  const destaques = useMemo(() => {
    const mapa = new Map<number, Destaque>()
    if (!detalhe || selecionadaId == null || detalhe.disciplina.id !== selecionadaId) return mapa
    for (const i of detalhe.cadeiaAntes) mapa.set(i.id, i.nivel === 1 ? 'pre' : 'pre-indireto')
    for (const i of detalhe.cadeiaDepois) mapa.set(i.id, i.nivel === 1 ? 'libera' : 'libera-indireto')
    mapa.set(selecionadaId, 'selecionada')
    return mapa
  }, [detalhe, selecionadaId])

  function abrir(id: number) {
    setSelecionadaId(id)
    setDetalhe(null)
    void carregarDetalhe(id)
  }

  // Depois de marcar ou desmarcar, recarrega estados, resumo e a cadeia (com os estados novos).
  async function depoisDeMarcar(id: number) {
    await carregar()
    await carregarDetalhe(id)
  }

  async function alternarRapido(d: Disciplina) {
    setAvisoRapido(null)
    try {
      if (d.estado === 'CURSADA') await api.desmarcar(alunoId, d.id)
      else await api.marcar(alunoId, d.id, 'CURSADA')
      await carregar()
    } catch (e) {
      setAvisoRapido(e instanceof ErroApi ? e.message : 'Não foi possível marcar')
    }
  }

  if (erro && !dados) {
    return (
      <main className="pagina estreita">
        <p className="alerta erro">{erro}</p>
        <button type="button" className="botao secundario" onClick={() => void carregar()}>
          Tentar de novo
        </button>
      </main>
    )
  }
  if (!dados) return <main className="pagina"><p className="carregando">Carregando sua matriz…</p></main>

  const { fluxo, resumo } = dados
  const termo = busca.trim().toLowerCase()
  const confere = (d: Disciplina) =>
    !termo || d.codigo.toLowerCase().includes(termo) || d.nome.toLowerCase().includes(termo)
  const emFoco = destaques.size > 0
  const selecionada = selecionadaId != null ? porId.get(selecionadaId) : undefined
  const semMarcacoes = resumo.cursadas + resumo.cursando === 0
  const obrigatorias = fluxo.semestres.reduce((n, s) => n + s.disciplinas.length, 0)

  const cartao = (d: Disciplina) => (
    <CartaoDisciplina
      key={d.id}
      disciplina={d}
      destaque={destaques.get(d.id)}
      esmaecida={(emFoco && !destaques.has(d.id)) || !confere(d)}
      aoClicar={() => (marcacaoRapida ? void alternarRapido(d) : abrir(d.id))}
    />
  )

  return (
    <main className={`pagina matriz ${selecionada ? 'com-painel' : ''}`}>
      <section className="cabecalho-matriz">
        <div>
          <p className="sobretitulo">Seu percurso em perspectiva</p>
          <h1>Minha matriz curricular</h1>
          <p className="texto-apoio">Veja onde você está e quais caminhos já pode seguir.</p>
        </div>
        {semMarcacoes && (
          <button type="button" className="botao secundario" onClick={aoAlterarFluxo}>
            Trocar o fluxo escolhido
          </button>
        )}
      </section>

      <Resumo resumo={resumo} />

      {semMarcacoes && (
        <p className="alerta info">
          <strong>Passo 2 de 2:</strong> marque as disciplinas que você já cursou. Ligue a <em>marcação rápida</em> e
          clique nos cartões, ou abra cada disciplina para ver os detalhes.
        </p>
      )}

      <section className="quadro">
        <div className="quadro-topo">
          <p>
            <strong>Fluxo {fluxo.nome}</strong>
            <span className={`selo selo-${fluxo.rotulo.toLowerCase()}`}>{NOME_ROTULO[fluxo.rotulo]}</span>
            <span className="texto-apoio">
              {fluxo.semestres.length} semestres · {obrigatorias} obrigatórias · {fluxo.optativas.length} optativas ·{' '}
              {fluxo.curso.nome}
            </span>
          </p>
          <div className="quadro-ferramentas">
            <label className="interruptor">
              <input
                type="checkbox"
                checked={marcacaoRapida}
                onChange={(e) => {
                  setMarcacaoRapida(e.target.checked)
                  fechar()
                }}
              />
              <span>Marcação rápida de cursadas</span>
            </label>
            <input
              className="busca"
              type="search"
              placeholder="Buscar por nome ou código"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
            />
          </div>
        </div>

        <Legenda />

        {marcacaoRapida && (
          <p className="faixa rapida">
            Marcação rápida ligada: um clique marca a disciplina como cursada; outro clique desmarca.
            {avisoRapido && <strong> {avisoRapido}</strong>}
          </p>
        )}
        {emFoco && selecionada && (
          <div className="faixa foco">
            <span>
              Modo foco · cadeia de <strong>{selecionada.codigo}</strong> — {selecionada.nome}
            </span>
            <span className="faixa-legenda">
              <i className="chave pre" /> pré-requisito direto <i className="chave pre-indireto" /> indireto{' '}
              <i className="chave libera" /> libera
            </span>
            <button type="button" className="botao-link" onClick={fechar}>
              Sair do foco
            </button>
          </div>
        )}

        <div
          className="linhas"
          onClick={(e) => {
            if (!(e.target as HTMLElement).closest('.cartao')) fechar()
          }}
        >
          {fluxo.semestres.map((s) => (
            <div className="linha-semestre" key={s.numero}>
              <div className="rotulo-semestre">
                <h3>{ordinalSemestre(s.numero)}</h3>
                {s.titulo && <p>{s.titulo}</p>}
                <small>
                  {s.disciplinas.length} {s.disciplinas.length === 1 ? 'disciplina' : 'disciplinas'}
                </small>
              </div>
              <div className="cartoes">{s.disciplinas.map(cartao)}</div>
            </div>
          ))}

          {fluxo.optativas.length > 0 && (
            <div className="linha-semestre optativas">
              <div className="rotulo-semestre">
                <h3>Optativas</h3>
                <p>
                  {resumo.creditosOptativosCursados} de {fluxo.creditosOptativosExigidos} créditos
                </p>
                <small>{fluxo.optativas.length} no catálogo</small>
              </div>
              <div className="cartoes">{fluxo.optativas.map(cartao)}</div>
            </div>
          )}

          {fluxo.externas.length > 0 && (
            <div className="linha-semestre externas">
              <div className="rotulo-semestre">
                <h3>Externos</h3>
                <p>Pré-requisitos de fora do fluxo</p>
                <small>não contam no progresso</small>
              </div>
              <div className="cartoes">{fluxo.externas.map(cartao)}</div>
            </div>
          )}
        </div>
      </section>

      {selecionada && (
        <PainelDisciplina
          key={selecionada.id}
          disciplina={selecionada}
          detalhe={detalhe?.disciplina.id === selecionada.id ? detalhe : null}
          aoFechar={fechar}
          aoAbrir={abrir}
          aoMarcar={(situacao) =>
            api.marcar(alunoId, selecionada.id, situacao).then(() => depoisDeMarcar(selecionada.id))
          }
          aoDesmarcar={() => api.desmarcar(alunoId, selecionada.id).then(() => depoisDeMarcar(selecionada.id))}
        />
      )}
    </main>
  )
}

function Resumo({ resumo: r }: { resumo: ResumoProgresso }) {
  return (
    <section className="resumo" aria-label="Resumo do progresso">
      <div className="resumo-progresso">
        <span className="resumo-rotulo">Percurso concluído</span>
        <strong>{r.percentualConcluido}%</strong>
        <div className="barra" role="progressbar" aria-valuenow={r.percentualConcluido} aria-valuemin={0} aria-valuemax={100}>
          <span style={{ width: `${r.percentualConcluido}%` }} />
        </div>
        <small>
          {r.creditosConcluidos} de {r.totalCreditosExigidos} créditos · {r.obrigatoriasCursadas} de{' '}
          {r.obrigatoriasTotal} obrigatórias
        </small>
      </div>
      <Numero valor={r.cursadas} rotulo="cursadas" apoio={`${r.cargaHorariaConcluida} h concluídas`} estado="cursada" />
      <Numero valor={r.cursando} rotulo="cursando" apoio="neste semestre" estado="cursando" />
      <Numero valor={r.disponiveis} rotulo="disponíveis" apoio="pré-requisitos cumpridos" estado="disponivel" />
      <Numero
        valor={r.creditosOptativosCursados}
        rotulo={`de ${r.creditosOptativosExigidos} créditos optativos`}
        apoio={r.creditosOptativosFaltantes ? `faltam ${r.creditosOptativosFaltantes}` : 'exigência cumprida'}
        estado="optativa"
      />
    </section>
  )
}

function Numero({ valor, rotulo, apoio, estado }: { valor: number; rotulo: string; apoio: string; estado: string }) {
  return (
    <div className={`resumo-numero ${estado}`}>
      <strong>{String(valor).padStart(2, '0')}</strong>
      <span>{rotulo}</span>
      <small>{apoio}</small>
    </div>
  )
}

function Legenda() {
  return (
    <ul className="legenda" aria-label="Legenda">
      <li className="estado-cursada">
        <span className="ponto" /> Cursada
      </li>
      <li className="estado-cursando">
        <span className="ponto" /> Cursando
      </li>
      <li className="estado-disponivel">
        <span className="ponto" /> Disponível
      </li>
      <li className="estado-bloqueada">
        <span className="ponto" /> Bloqueada
      </li>
      <li className="tracejada">Optativa</li>
      <li className="texto-apoio">Disponível = todos os pré-requisitos cursados. Clique numa disciplina para ver a cadeia.</li>
    </ul>
  )
}
