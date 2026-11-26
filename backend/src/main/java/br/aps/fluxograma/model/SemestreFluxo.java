package br.aps.fluxograma.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/** Entidade fraca (fluxo, número) com título opcional ("Fundamentos", "Bases"…). */
@Entity
@Table(name = "semestre_fluxo")
public class SemestreFluxo {

	@EmbeddedId
	private SemestreFluxoId id;

	@MapsId("fluxoId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fluxo_id")
	private Fluxo fluxo;

	@Column
	private String titulo;

	protected SemestreFluxo() {
	}

	public SemestreFluxo(Fluxo fluxo, int numero, String titulo) {
		this.id = new SemestreFluxoId(fluxo.getId(), numero);
		this.fluxo = fluxo;
		this.titulo = titulo;
	}

	public SemestreFluxoId getId() {
		return id;
	}

	public int getNumero() {
		return id.getNumero();
	}

	public Fluxo getFluxo() {
		return fluxo;
	}

	public String getTitulo() {
		return titulo;
	}
}
