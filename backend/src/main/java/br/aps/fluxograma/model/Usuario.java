package br.aps.fluxograma.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Credenciais comuns aos três atores. Cada papel tem a sua tabela (aluno, coordenador,
 * admin_institucional) e a coluna papel distingue o tipo.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "papel", discriminatorType = DiscriminatorType.STRING, length = 30)
public abstract class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String nome;

	@Column(name = "senha_hash", nullable = false)
	private String senhaHash;

	protected Usuario() {
	}

	protected Usuario(String email, String nome, String senhaHash) {
		this.email = email;
		this.nome = nome;
		this.senhaHash = senhaHash;
	}

	public abstract Papel getPapel();

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getNome() {
		return nome;
	}

	public String getSenhaHash() {
		return senhaHash;
	}
}
