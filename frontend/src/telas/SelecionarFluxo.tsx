import { useEffect, useState } from 'react'
import { api, ErroApi } from '../api'
import { NOME_ROTULO } from '../rotulos'
import type { Curso, FluxoResumo, Usuario } from '../tipos'

/** HU06: escolher o curso (só quando há mais de um) e o fluxo. */
export function SelecionarFluxo({
  usuario,
  aoSelecionar,
  aoVoltar,
}: {
  usuario: Usuario
  aoSelecionar: (fluxoId: number) => void
  aoVoltar?: () => void
}) {
  const [cursos, setCursos] = useState<Curso[] | null>(null)
  const [cursoId, setCursoId] = useState<number | null>(null)
  const [fluxos, setFluxos] = useState<FluxoResumo[] | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [salvando, setSalvando] = useState<number | null>(null)

  useEffect(() => {
    api
      .cursos()
      .then((lista) => {
        setCursos(lista)
        if (lista.length === 1) setCursoId(lista[0].id)
      })
      .catch((e) => setErro(e instanceof ErroApi ? e.message : 'Erro ao carregar os cursos'))
  }, [])

  useEffect(() => {
    if (cursoId == null) return
    setFluxos(null)
    api
      .fluxos(cursoId)
      .then((lista) =>
        // Só publicados e já vigentes ou em acompanhamento; prévia fica para a simulação de troca (HU12).
        setFluxos(lista.filter((f) => f.rotulo === 'VIGENTE' || f.rotulo === 'EM_ACOMPANHAMENTO')),
      )
      .catch((e) => setErro(e instanceof ErroApi ? e.message : 'Erro ao carregar os fluxos'))
  }, [cursoId])

  async function selecionar(fluxoId: number) {
    setErro(null)
    setSalvando(fluxoId)
    try {
      await api.selecionarFluxo(usuario.id, fluxoId)
      aoSelecionar(fluxoId)
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível selecionar o fluxo')
    } finally {
      setSalvando(null)
    }
  }

  const curso = cursos?.find((c) => c.id === cursoId)

  return (
    <main className="pagina estreita">
      {aoVoltar && (
        <button type="button" className="botao-link voltar" onClick={aoVoltar}>
          ← Voltar para a matriz
        </button>
      )}
      <p className="sobretitulo">{aoVoltar ? 'Trocar o fluxo escolhido' : 'Primeiro acesso · passo 1 de 2'}</p>
      <h1>Qual é o seu fluxo?</h1>
      <p className="texto-apoio">
        O fluxo é a matriz curricular que você segue. Depois de escolher, marque as disciplinas que já cursou. Enquanto
        nenhuma disciplina estiver marcada, dá para voltar e trocar.
      </p>

      {erro && (
        <p className="alerta erro" role="alert">
          {erro}
        </p>
      )}

      {cursos && cursos.length > 1 && (
        <div className="lista-cursos">
          {cursos.map((c) => (
            <button
              key={c.id}
              type="button"
              className={`opcao-curso ${c.id === cursoId ? 'escolhida' : ''}`}
              onClick={() => setCursoId(c.id)}
            >
              <span className="codigo">{c.codigo}</span> {c.nome}
            </button>
          ))}
        </div>
      )}

      {curso && <h2 className="secao-titulo">{curso.nome}</h2>}
      {cursoId != null && fluxos == null && !erro && <p className="carregando">Carregando fluxos…</p>}
      {fluxos && fluxos.length === 0 && <p className="texto-apoio">Nenhum fluxo disponível para este curso.</p>}

      <div className="lista-fluxos">
        {fluxos?.map((f) => {
          const atual = usuario.fluxoId === f.id
          return (
            <article key={f.id} className={`cartao-fluxo ${atual ? 'atual' : ''}`}>
              <div>
                <span className={`selo selo-${f.rotulo.toLowerCase()}`}>{NOME_ROTULO[f.rotulo]}</span>
                <h3>Fluxo {f.nome}</h3>
                <p className="texto-apoio">Vigência a partir de {f.vigenciaInicio}</p>
              </div>
              <dl className="numeros-fluxo">
                <div>
                  <dt>Semestres</dt>
                  <dd>{f.quantidadeSemestres}</dd>
                </div>
                <div>
                  <dt>Obrigatórias</dt>
                  <dd>{f.quantidadeObrigatorias}</dd>
                </div>
                <div>
                  <dt>Optativas no catálogo</dt>
                  <dd>{f.quantidadeOptativas}</dd>
                </div>
                <div>
                  <dt>Créditos exigidos</dt>
                  <dd>{f.totalCreditosExigidos}</dd>
                </div>
              </dl>
              <button
                type="button"
                className="botao primario"
                disabled={salvando != null}
                onClick={() => (atual && aoVoltar ? aoVoltar() : void selecionar(f.id))}
              >
                {atual ? 'Fluxo atual' : salvando === f.id ? 'Salvando…' : 'Usar este fluxo'}
              </button>
            </article>
          )
        })}
      </div>
    </main>
  )
}
