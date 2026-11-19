package com.biblioteca.controller;

import com.biblioteca.model.Autor;
import com.biblioteca.repository.AutorRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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

/**
 * API REST de Autores.
 *
 * POST   /api/autores       -> cria (201)
 * GET    /api/autores       -> lista ordenado por nome (200)
 * GET    /api/autores/{id}  -> busca (200 / 404)
 * PUT    /api/autores/{id}  -> atualiza (200 / 404)
 * DELETE /api/autores/{id}  -> remove (200 / 404; 409 se o autor tiver livros)
 */
@RestController
@RequestMapping("/api/autores")
@CrossOrigin(origins = "*")
public class AutorController {

    private final AutorRepository autorRepository;

    public AutorController(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    @PostMapping
    public ResponseEntity<Autor> criar(@Valid @RequestBody Autor autor) {
        autor.setId(null); // o id e sempre gerado pelo banco
        Autor salvo = autorRepository.save(autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public ResponseEntity<List<Autor>> listar() {
        return ResponseEntity.ok(autorRepository.findAllByOrderByNomeAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autor> buscar(@PathVariable Long id) {
        return autorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Autor> atualizar(@PathVariable Long id, @Valid @RequestBody Autor dados) {
        return autorRepository.findById(id)
                .map(autor -> {
                    autor.setNome(dados.getNome());
                    autor.setNacionalidade(dados.getNacionalidade());
                    autor.setDataNascimento(dados.getDataNascimento());
                    return ResponseEntity.ok(autorRepository.save(autor));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!autorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            autorRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException e) {
            // A FK autor_id em livros impede excluir um autor que ainda tem livros
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "Não é possível excluir o autor: existem livros vinculados a ele. Exclua os livros antes."));
        }
    }
}
