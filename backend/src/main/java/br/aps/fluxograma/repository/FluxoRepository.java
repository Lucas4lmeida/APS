package br.aps.fluxograma.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.aps.fluxograma.model.Fluxo;

public interface FluxoRepository extends JpaRepository<Fluxo, Long> {

	List<Fluxo> findByCursoIdOrderByVigenciaInicioDesc(Long cursoId);

	Optional<Fluxo> findByCursoIdAndNome(Long cursoId, String nome);
}
