package com.biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade Livro -> tabela "livros" (lado N do relacionamento Autor 1 : N Livro).
 * A chave estrangeira autor_id fica aqui, assim como contato_id fica em compromissos na Agenda.
 */
@Entity
@Table(name = "livros")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
    @Column(nullable = false, length = 200)
    private String titulo;

    @Size(max = 20, message = "O ISBN deve ter no máximo 20 caracteres")
    @Column(length = 20)
    private String isbn;

    @Column(name = "ano_publicacao")
    private Integer anoPublicacao;

    @Size(max = 50, message = "O gênero deve ter no máximo 50 caracteres")
    @Column(length = 50)
    private String genero;

    /** Varios livros -> um autor. No JSON basta enviar "autor": {"id": 1}. */
    @NotNull(message = "O autor é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autor_id", nullable = false) // cria a coluna FK
    private Autor autor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Preenche a data de criacao automaticamente antes de inserir no banco. */
    @PrePersist
    protected void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }
}
