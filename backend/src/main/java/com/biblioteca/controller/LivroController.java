package com.biblioteca.controller;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.LivroRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API REST de Livros.
 *
 * POST   /api/livros                  -> cria um livro ligado a um autor (201)
 * GET    /api/livros                  -> lista ordenado por titulo (200)
 * GET    /api/livros/{id}             -> busca (200 / 404)
 * PUT    /api/livros/{id}             -> atualiza, inclusive o autor (200 / 404)
 * DELETE /api/livros/{id}             -> remove (200 / 404)
 * GET    /api/livros/autor/{autorId}  -> lista os livros de um autor (200)
 *
 * O autor vai no JSON so com o id: "autor": {"id": 1}.
 * Se o autor nao existir, devolve 400 com uma mensagem.
 */
@RestController
@RequestMapping("/api/livros")
@CrossOrigin(origins = "*")
public class LivroController {

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;

    public LivroController(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody Livro livro) {
        Optional<Autor> autor = buscarAutor(livro);
        if (autor.isEmpty()) {
            return autorInvalido();
        }
        livro.setId(null); // o id e sempre gerado pelo banco
        livro.setAutor(autor.get());
        Livro salvo = livroRepository.save(livro);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public ResponseEntity<List<Livro>> listar() {
        return ResponseEntity.ok(livroRepository.findAllByOrderByTituloAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscar(@PathVariable Long id) {
        return livroRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<Livro>> listarPorAutor(@PathVariable Long autorId) {
        return ResponseEntity.ok(livroRepository.findByAutorId(autorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody Livro dados) {
        Optional<Livro> existente = livroRepository.findById(id);
        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<Autor> autor = buscarAutor(dados);
        if (autor.isEmpty()) {
            return autorInvalido();
        }
        Livro livro = existente.get();
        livro.setTitulo(dados.getTitulo());
        livro.setIsbn(dados.getIsbn());
        livro.setAnoPublicacao(dados.getAnoPublicacao());
        livro.setGenero(dados.getGenero());
        livro.setAutor(autor.get()); // atualiza tambem o autor (igual ao setContato da Agenda)
        return ResponseEntity.ok(livroRepository.save(livro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!livroRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        livroRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /** Carrega do banco o autor informado no JSON ("autor": {"id": ...}). */
    private Optional<Autor> buscarAutor(Livro livro) {
        if (livro.getAutor() == null || livro.getAutor().getId() == null) {
            return Optional.empty();
        }
        return autorRepository.findById(livro.getAutor().getId());
    }

    private ResponseEntity<Map<String, String>> autorInvalido() {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Autor não encontrado. Informe um autor existente: \"autor\": {\"id\": ...}"));
    }
}
