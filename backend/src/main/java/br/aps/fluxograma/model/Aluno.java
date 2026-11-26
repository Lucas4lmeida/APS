package br.aps.fluxograma.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/** Aluno: matrícula única e fluxo opcional (0..1) até selecionar o fluxo (HU06). */
@Entity
@Table(name = "aluno")
@DiscriminatorValue("ALUNO")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Aluno extends Usuario {

	@Column(nullable = false, unique = true)
	private String matricula;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fluxo_id")
	private Fluxo fluxo;

	protected Aluno() {
	}

	public Aluno(String email, String nome, String senhaHash, String matricula) {
		super(email, nome, senhaHash);
		this.matricula = matricula;
	}

	@Override
	public Papel getPapel() {
		return Papel.ALUNO;
	}

	public String getMatricula() {
		return matricula;
	}

	public Fluxo getFluxo() {
		return fluxo;
	}

	public void setFluxo(Fluxo fluxo) {
		this.fluxo = fluxo;
	}
}
