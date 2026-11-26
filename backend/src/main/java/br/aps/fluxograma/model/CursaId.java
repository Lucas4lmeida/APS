package br.aps.fluxograma.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CursaId implements Serializable {

	@Column(name = "aluno_id")
	private Long alunoId;

	@Column(name = "disciplina_id")
	private Long disciplinaId;

	protected CursaId() {
	}

	public CursaId(Long alunoId, Long disciplinaId) {
		this.alunoId = alunoId;
		this.disciplinaId = disciplinaId;
	}

	public Long getAlunoId() {
		return alunoId;
	}

	public Long getDisciplinaId() {
		return disciplinaId;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof CursaId outro && Objects.equals(alunoId, outro.alunoId)
				&& Objects.equals(disciplinaId, outro.disciplinaId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(alunoId, disciplinaId);
	}
}
