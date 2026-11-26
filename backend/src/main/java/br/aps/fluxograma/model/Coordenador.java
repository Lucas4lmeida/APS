package br.aps.fluxograma.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/** Coordenador: vinculado a exatamente um curso (HU01). */
@Entity
@Table(name = "coordenador")
@DiscriminatorValue("COORDENADOR")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Coordenador extends Usuario {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "curso_id", nullable = false)
	private Curso curso;

	protected Coordenador() {
	}

	public Coordenador(String email, String nome, String senhaHash, Curso curso) {
		super(email, nome, senhaHash);
		this.curso = curso;
	}

	@Override
	public Papel getPapel() {
		return Papel.COORDENADOR;
	}

	public Curso getCurso() {
		return curso;
	}
}
