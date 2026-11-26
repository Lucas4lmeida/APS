package br.aps.fluxograma.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.aps.fluxograma.model.Coordenador;

public interface CoordenadorRepository extends JpaRepository<Coordenador, Long> {

	@Query("select c.curso.id from Coordenador c where c.id = :coordenadorId")
	Optional<Long> findCursoIdByCoordenadorId(Long coordenadorId);
}
