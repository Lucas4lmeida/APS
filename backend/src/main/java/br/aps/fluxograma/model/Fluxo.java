package br.aps.fluxograma.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Uma versão da matriz curricular de um curso (ex.: 2023.1). */
@Entity
@Table(name = "fluxo")
public class Fluxo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "curso_id", nullable = false)
	private Curso curso;

	@Column(nullable = false)
	private String nome;

	/** Semestre letivo em que a matriz passa a valer (ex.: 2023.1). */
	@Column(name = "vigencia_inicio", nullable = false)
	private String vigenciaInicio;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusFluxo status;

	@Column(name = "creditos_optativos_exigidos", nullable = false)
	private int creditosOptativosExigidos;

	protected Fluxo() {
	}

	public Fluxo(Curso curso, String nome, String vigenciaInicio, StatusFluxo status, int creditosOptativosExigidos) {
		this.curso = curso;
		this.nome = nome;
		this.vigenciaInicio = vigenciaInicio;
		this.status = status;
		this.creditosOptativosExigidos = creditosOptativosExigidos;
	}

	public Long getId() {
		return id;
	}

	public Curso getCurso() {
		return curso;
	}

	public String getNome() {
		return nome;
	}

	public String getVigenciaInicio() {
		return vigenciaInicio;
	}

	public StatusFluxo getStatus() {
		return status;
	}

	public boolean isPublicado() {
		return status == StatusFluxo.PUBLICADO;
	}

	public int getCreditosOptativosExigidos() {
		return creditosOptativosExigidos;
	}
}
