package br.aps.fluxograma.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_institucional")
@DiscriminatorValue("ADMIN_INSTITUCIONAL")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class AdminInstitucional extends Usuario {

	protected AdminInstitucional() {
	}

	public AdminInstitucional(String email, String nome, String senhaHash) {
		super(email, nome, senhaHash);
	}

	@Override
	public Papel getPapel() {
		return Papel.ADMIN_INSTITUCIONAL;
	}
}
