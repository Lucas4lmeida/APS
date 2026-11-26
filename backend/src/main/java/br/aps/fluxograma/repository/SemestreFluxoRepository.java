package br.aps.fluxograma.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.aps.fluxograma.model.SemestreFluxo;
import br.aps.fluxograma.model.SemestreFluxoId;

public interface SemestreFluxoRepository extends JpaRepository<SemestreFluxo, SemestreFluxoId> {

	List<SemestreFluxo> findByIdFluxoIdOrderByIdNumeroAsc(Long fluxoId);

	/** Quantidade de semestres de cada fluxo do curso: [fluxoId, quantidade]. */
	@Query("select s.id.fluxoId, count(s) from SemestreFluxo s where s.fluxo.curso.id = :cursoId group by s.id.fluxoId")
	List<Object[]> contarPorFluxoDoCurso(Long cursoId);
}
