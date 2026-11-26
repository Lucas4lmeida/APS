package br.aps.fluxograma.model;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "disciplina")
public class Disciplina {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fluxo_id", nullable = false)
	private Fluxo fluxo;

	@Column(nullable = false)
	private String codigo;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	private int creditos;

	@Column(name = "carga_horaria", nullable = false)
	private int cargaHoraria;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoDisciplina tipo;

	/** Número do semestre no fluxo; só as obrigatórias têm. */
	@Column
	private Integer semestre;

	/** Posição lado a lado na linha do semestre (ou no catálogo de optativas). */
	@Column(nullable = false)
	private int ordem;

	/** Pré-requisito externo vindo de um código não encontrado no CSV, ainda não confirmado. */
	@Column(nullable = false)
	private boolean pendente;

	/** Relação "exige": todos precisam estar cumpridos (relação "E"). */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "pre_requisito", joinColumns = @JoinColumn(name = "disciplina_id"),
			inverseJoinColumns = @JoinColumn(name = "requisito_id"))
	private Set<Disciplina> preRequisitos = new LinkedHashSet<>();

	protected Disciplina() {
	}

	public Disciplina(Fluxo fluxo, String codigo, String nome, int creditos, int cargaHoraria, TipoDisciplina tipo,
			Integer semestre, int ordem, boolean pendente) {
		this.fluxo = fluxo;
		this.codigo = codigo;
		this.nome = nome;
		this.creditos = creditos;
		this.cargaHoraria = cargaHoraria;
		this.tipo = tipo;
		this.semestre = semestre;
		this.ordem = ordem;
		this.pendente = pendente;
	}

	public Long getId() {
		return id;
	}

	public Fluxo getFluxo() {
		return fluxo;
	}

	public String getCodigo() {
		return codigo;
	}

	public String getNome() {
		return nome;
	}

	public int getCreditos() {
		return creditos;
	}

	public int getCargaHoraria() {
		return cargaHoraria;
	}

	public TipoDisciplina getTipo() {
		return tipo;
	}

	public Integer getSemestre() {
		return semestre;
	}

	public int getOrdem() {
		return ordem;
	}

	public boolean isPendente() {
		return pendente;
	}

	public Set<Disciplina> getPreRequisitos() {
		return preRequisitos;
	}
}
