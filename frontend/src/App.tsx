import { useCallback, useEffect, useState } from 'react'
import { definirAoExpirar, lerSessao, salvarSessao } from './api'
import type { Sessao } from './tipos'
import { Acesso } from './telas/Acesso'
import { SelecionarFluxo } from './telas/SelecionarFluxo'
import { Matriz } from './telas/Matriz'
import { Topo } from './componentes/Topo'

export function App() {
  const [sessao, setSessao] = useState<Sessao | null>(() => lerSessao())
  const [trocandoFluxo, setTrocandoFluxo] = useState(false)

  const atualizarSessao = useCallback((nova: Sessao | null) => {
    salvarSessao(nova)
    setSessao(nova)
    setTrocandoFluxo(false)
  }, [])

  useEffect(() => definirAoExpirar(() => atualizarSessao(null)), [atualizarSessao])

  if (!sessao) return <Acesso aoEntrar={atualizarSessao} />

  const sair = () => atualizarSessao(null)
  const { usuario } = sessao

  if (usuario.papel !== 'ALUNO') {
    return (
      <>
        <Topo usuario={usuario} aoSair={sair} mostrarNavegacao={false} />
        <main className="pagina estreita">
          <p className="sobretitulo">Área em construção</p>
          <h1>Olá, {usuario.nome}</h1>
          <p className="texto-apoio">
            Este MVP cobre a jornada do aluno. As telas do coordenador e do admin institucional entram nas próximas
            etapas; a API já reconhece o seu papel.
          </p>
        </main>
      </>
    )
  }

  if (!usuario.fluxoId || trocandoFluxo) {
    return (
      <>
        <Topo usuario={usuario} aoSair={sair} mostrarNavegacao={false} />
        <SelecionarFluxo
          usuario={usuario}
          aoVoltar={usuario.fluxoId ? () => setTrocandoFluxo(false) : undefined}
          aoSelecionar={(fluxoId) => atualizarSessao({ ...sessao, usuario: { ...usuario, fluxoId } })}
        />
      </>
    )
  }

  return (
    <>
      <Topo usuario={usuario} aoSair={sair} mostrarNavegacao />
      <Matriz alunoId={usuario.id} aoAlterarFluxo={() => setTrocandoFluxo(true)} />
    </>
  )
}
