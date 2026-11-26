package br.aps.fluxograma.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Marcação do aluno numa disciplina: Cursada ou Cursando. Desmarcar = apagar a linha (HU19). */
@Entity
@Table(name = "cursa")
public class Cursa {

	@EmbeddedId
	private CursaId id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Situacao situacao;

	protected Cursa() {
	}

	public Cursa(Long alunoId, Long disciplinaId, Situacao situacao) {
		this.id = new CursaId(alunoId, disciplinaId);
		this.situacao = situacao;
	}

	public CursaId getId() {
		return id;
	}

	public Long getDisciplinaId() {
		return id.getDisciplinaId();
	}

	public Situacao getSituacao() {
		return situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}
}
