package br.aps.fluxograma.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.aps.fluxograma.model.Disciplina;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

	/** Todas as disciplinas do fluxo já com os pré-requisitos carregados (sem N+1). */
	@Query("select distinct d from Disciplina d left join fetch d.preRequisitos where d.fluxo.id = :fluxoId")
	List<Disciplina> findByFluxoComPreRequisitos(Long fluxoId);

	Optional<Disciplina> findByFluxoIdAndCodigo(Long fluxoId, String codigo);

	/** Quantidade e créditos por tipo em cada fluxo do curso: [fluxoId, tipo, quantidade, créditos]. */
	@Query("""
			select d.fluxo.id, d.tipo, count(d), coalesce(sum(d.creditos), 0)
			from Disciplina d
			where d.fluxo.curso.id = :cursoId
			group by d.fluxo.id, d.tipo
			""")
	List<Object[]> totaisPorFluxoDoCurso(Long cursoId);
}
