package br.aps.fluxograma.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SemestreFluxoId implements Serializable {

	@Column(name = "fluxo_id")
	private Long fluxoId;

	@Column(name = "numero")
	private Integer numero;

	protected SemestreFluxoId() {
	}

	public SemestreFluxoId(Long fluxoId, Integer numero) {
		this.fluxoId = fluxoId;
		this.numero = numero;
	}

	public Long getFluxoId() {
		return fluxoId;
	}

	public Integer getNumero() {
		return numero;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SemestreFluxoId outro && Objects.equals(fluxoId, outro.fluxoId)
				&& Objects.equals(numero, outro.numero);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fluxoId, numero);
	}
}
