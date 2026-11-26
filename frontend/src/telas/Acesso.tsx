import { useState, type ChangeEvent, type FormEvent, type InputHTMLAttributes } from 'react'
import { api, ErroApi } from '../api'
import type { Sessao } from '../tipos'
import { Marca } from '../componentes/Topo'

// Conta criada pelos dados de desenvolvimento do back-end (perfil "dev"). Defina VITE_DEMO=nao para esconder.
const DEMO = { login: '2023100001', senha: 'aluno123' }
const MOSTRAR_DEMO = import.meta.env.VITE_DEMO !== 'nao'

export function Acesso({ aoEntrar }: { aoEntrar: (sessao: Sessao) => void }) {
  const [modo, setModo] = useState<'login' | 'cadastro'>('login')

  return (
    <div className="acesso">
      <div className="acesso-cartao">
        <div className="acesso-marca">
          <Marca />
        </div>
        {modo === 'login' ? (
          <Login aoEntrar={aoEntrar} aoCriarConta={() => setModo('cadastro')} />
        ) : (
          <Cadastro aoEntrar={aoEntrar} aoVoltar={() => setModo('login')} />
        )}
      </div>
      <p className="acesso-rodape">
        Nesta versão o sistema não se integra ao SISACAD: você informa o próprio histórico e a matrícula é só o
        planejamento.
      </p>
    </div>
  )
}

function Login({ aoEntrar, aoCriarConta }: { aoEntrar: (s: Sessao) => void; aoCriarConta: () => void }) {
  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function entrar(dados: { login: string; senha: string }) {
    setErro(null)
    setEnviando(true)
    try {
      aoEntrar(await api.login(dados.login.trim(), dados.senha))
    } catch (e) {
      setErro(e instanceof ErroApi ? e.message : 'Não foi possível entrar')
    } finally {
      setEnviando(false)
    }
  }

  function enviar(evento: FormEvent) {
    evento.preventDefault()
    void entrar({ login, senha })
  }

  return (
    <form className="formulario" onSubmit={enviar} noValidate>
      <p className="sobretitulo">Acesso</p>
      <h1>Entrar</h1>
      <p className="texto-apoio">Use o e-mail ou a matrícula e a sua senha.</p>

      <label className="campo">
        <span>E-mail ou matrícula</span>
        <input value={login} onChange={(e) => setLogin(e.target.value)} autoComplete="username" required />
      </label>
      <label className="campo">
        <span>Senha</span>
        <input
          type="password"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          autoComplete="current-password"
          required
        />
      </label>

      {erro && (
        <p className="alerta erro" role="alert">
          {erro}
        </p>
      )}

      <button className="botao primario largo" type="submit" disabled={enviando || !login || !senha}>
        {enviando ? 'Entrando…' : 'Entrar'}
      </button>
      <p className="alternativa">
        Primeiro acesso?{' '}
        <button type="button" className="botao-link" onClick={aoCriarConta}>
          Criar conta
        </button>
      </p>

      {MOSTRAR_DEMO && (
        <div className="demo">
          <p>
            <strong>Conta de demonstração</strong> (dados de desenvolvimento): matrícula {DEMO.login}, senha{' '}
            {DEMO.senha}. A aluna já concluiu os semestres 1 a 4.
          </p>
          <button type="button" className="botao secundario" disabled={enviando} onClick={() => void entrar(DEMO)}>
            Entrar como Ana Costa
          </button>
        </div>
      )}
    </form>
  )
}

type CamposCadastro = 'matricula' | 'nome' | 'email' | 'senha' | 'confirmacao'

function Cadastro({ aoEntrar, aoVoltar }: { aoEntrar: (s: Sessao) => void; aoVoltar: () => void }) {
  const [dados, setDados] = useState<Record<CamposCadastro, string>>({
    matricula: '',
    nome: '',
    email: '',
    senha: '',
    confirmacao: '',
  })
  const [erros, setErros] = useState<Partial<Record<CamposCadastro, string>>>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const mudar = (campo: CamposCadastro) => (e: ChangeEvent<HTMLInputElement>) =>
    setDados((d) => ({ ...d, [campo]: e.target.value }))

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErroGeral(null)
    const locais: Partial<Record<CamposCadastro, string>> = {}
    if (dados.senha.length < 8) locais.senha = 'A senha precisa ter pelo menos 8 caracteres'
    if (dados.confirmacao !== dados.senha) locais.confirmacao = 'As senhas não conferem'
    setErros(locais)
    if (Object.keys(locais).length) return

    setEnviando(true)
    try {
      aoEntrar(
        await api.registrar({
          matricula: dados.matricula.trim(),
          nome: dados.nome.trim(),
          email: dados.email.trim(),
          senha: dados.senha,
        }),
      )
    } catch (e) {
      if (e instanceof ErroApi) {
        const doServidor: Partial<Record<CamposCadastro, string>> = {}
        for (const campo of ['matricula', 'nome', 'email', 'senha'] as const) {
          const msg = e.doCampo(campo)
          if (msg) doServidor[campo] = msg
        }
        setErros(doServidor)
        setErroGeral(e.message)
      } else {
        setErroGeral('Não foi possível criar a conta')
      }
    } finally {
      setEnviando(false)
    }
  }

  const campo = (
    nome: CamposCadastro,
    rotulo: string,
    props: InputHTMLAttributes<HTMLInputElement> = {},
    dica?: string,
  ) => (
    <label className={`campo ${erros[nome] ? 'com-erro' : ''}`}>
      <span>{rotulo}</span>
      <input value={dados[nome]} onChange={mudar(nome)} required {...props} />
      {erros[nome] ? <small className="erro-campo">{erros[nome]}</small> : dica ? <small>{dica}</small> : null}
    </label>
  )

  return (
    <form className="formulario" onSubmit={enviar} noValidate>
      <p className="sobretitulo">Primeiro acesso</p>
      <h1>Crie sua conta</h1>
      <p className="texto-apoio">Você entra como aluno e escolhe o seu fluxo em seguida.</p>

      {campo('matricula', 'Matrícula', { autoComplete: 'off', inputMode: 'numeric' })}
      {campo('nome', 'Nome completo', { autoComplete: 'name' })}
      {campo('email', 'E-mail', { type: 'email', autoComplete: 'email' })}
      <div className="linha-campos">
        {campo('senha', 'Senha', { type: 'password', autoComplete: 'new-password' }, 'Mínimo de 8 caracteres')}
        {campo('confirmacao', 'Confirmar senha', { type: 'password', autoComplete: 'new-password' })}
      </div>

      {erroGeral && (
        <p className="alerta erro" role="alert">
          {erroGeral}
        </p>
      )}

      <button className="botao primario largo" type="submit" disabled={enviando}>
        {enviando ? 'Criando conta…' : 'Criar conta'}
      </button>
      <p className="alternativa">
        Já tem conta?{' '}
        <button type="button" className="botao-link" onClick={aoVoltar}>
          Entrar
        </button>
      </p>
    </form>
  )
}
