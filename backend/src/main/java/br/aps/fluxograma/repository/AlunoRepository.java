package br.aps.fluxograma.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.aps.fluxograma.model.Aluno;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

	Optional<Aluno> findByMatricula(String matricula);

	boolean existsByMatricula(String matricula);
}
