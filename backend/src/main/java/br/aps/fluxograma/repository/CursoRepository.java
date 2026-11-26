package br.aps.fluxograma.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.aps.fluxograma.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {

	Optional<Curso> findByCodigo(String codigo);

	List<Curso> findAllByOrderByNomeAsc();
}
