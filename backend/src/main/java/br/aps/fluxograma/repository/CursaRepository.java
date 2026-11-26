package br.aps.fluxograma.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.aps.fluxograma.model.Cursa;
import br.aps.fluxograma.model.CursaId;

public interface CursaRepository extends JpaRepository<Cursa, CursaId> {

	/** Marcações do aluno nas disciplinas de um fluxo (as de outros fluxos não são exibidas). */
	@Query("""
			select c from Cursa c
			where c.id.alunoId = :alunoId
			  and c.id.disciplinaId in (select d.id from Disciplina d where d.fluxo.id = :fluxoId)
			""")
	List<Cursa> findDoAlunoNoFluxo(Long alunoId, Long fluxoId);

	@Query("""
			select count(c) > 0 from Cursa c
			where c.id.alunoId = :alunoId
			  and c.id.disciplinaId in (select d.id from Disciplina d where d.fluxo.id = :fluxoId)
			""")
	boolean existeMarcacaoNoFluxo(Long alunoId, Long fluxoId);
}
