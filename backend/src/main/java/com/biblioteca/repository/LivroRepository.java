package com.biblioteca.repository;

import com.biblioteca.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {

    /** Lista todos os livros em ordem alfabetica de titulo. */
    List<Livro> findAllByOrderByTituloAsc();

    /** Lista os livros de um autor (consulta pela FK autor_id). */
    List<Livro> findByAutorId(Long autorId);
}
