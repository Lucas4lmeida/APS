import type { Usuario } from '../tipos'

// Navegação única do aluno (docs/prototipo-figma.md, item D10). Só a Matriz faz parte do MVP.
const DESTINOS = [
  'Matriz',
  'Simulação',
  'Matrícula do período',
  'Troca de fluxo',
  'Meus planejamentos',
  'Previsão de formatura',
]

export function Marca() {
  return (
    <div className="marca">
      <span className="marca-icone" aria-hidden>
        <svg viewBox="0 0 24 24" width="18" height="18">
          <path
            d="M4 6.5c2.8-1.1 5.4-1.1 8 .6v12c-2.6-1.7-5.2-1.7-8-.6zM20 6.5c-2.8-1.1-5.4-1.1-8 .6v12c2.6-1.7 5.2-1.7 8-.6z"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.6"
            strokeLinejoin="round"
          />
        </svg>
      </span>
      <span className="marca-nome">Fluxograma Acadêmico</span>
      <span className="marca-sub">Planejamento acadêmico</span>
    </div>
  )
}

export function Topo({
  usuario,
  aoSair,
  mostrarNavegacao,
}: {
  usuario: Usuario
  aoSair: () => void
  mostrarNavegacao: boolean
}) {
  const iniciais = usuario.nome
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join('')

  return (
    <header className="topo">
      <Marca />
      {mostrarNavegacao && (
        <nav className="destinos" aria-label="Navegação do aluno">
          {DESTINOS.map((destino, i) =>
            i === 0 ? (
              <span key={destino} className="destino ativo" aria-current="page">
                {destino}
              </span>
            ) : (
              <span key={destino} className="destino desativado" title="Em breve: fora do MVP">
                {destino}
              </span>
            ),
          )}
        </nav>
      )}
      <div className="perfil">
        <span className="avatar" aria-hidden>
          {iniciais}
        </span>
        <span className="perfil-texto">
          <strong>{usuario.nome}</strong>
          <small>{usuario.matricula ?? usuario.email}</small>
        </span>
        <button type="button" className="botao-link" onClick={aoSair}>
          Sair
        </button>
      </div>
    </header>
  )
}
