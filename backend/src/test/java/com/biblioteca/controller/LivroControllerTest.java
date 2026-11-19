package com.biblioteca.controller;

import com.biblioteca.model.Autor;
import com.biblioteca.model.Livro;
import com.biblioteca.repository.AutorRepository;
import com.biblioteca.repository.LivroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes unitarios do LivroController: so a camada web (MockMvc),
 * com os repositories substituidos por mocks do Mockito.
 */
@WebMvcTest(LivroController.class)
class LivroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LivroRepository livroRepository;

    @MockBean
    private AutorRepository autorRepository;

    private final Autor machado = new Autor(1L, "Machado de Assis", "Brasileira",
            LocalDate.of(1839, 6, 21), LocalDateTime.now());

    private final Autor clarice = new Autor(2L, "Clarice Lispector", "Brasileira",
            LocalDate.of(1920, 12, 10), LocalDateTime.now());

    private Livro novoLivro(Long id, String titulo, Autor autor) {
        return new Livro(id, titulo, "978-8535910667", 1899, "Romance", autor, LocalDateTime.now());
    }

    /** Monta o JSON do jeito que o frontend envia: o autor vai so com o id. */
    private String json(String titulo, Long autorId) throws Exception {
        Autor autor = autorId == null ? null : new Autor(autorId, null, null, null, null);
        return objectMapper.writeValueAsString(
                new Livro(null, titulo, "978-8535910667", 1899, "Romance", autor, null));
    }

    @Test
    void deveCriarLivroComSucesso() throws Exception {
        when(autorRepository.findById(1L)).thenReturn(Optional.of(machado));
        when(livroRepository.save(any(Livro.class))).thenReturn(novoLivro(10L, "Dom Casmurro", machado));

        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Dom Casmurro", 1L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.autor.id").value(1))
                .andExpect(jsonPath("$.autor.nome").value("Machado de Assis"));
    }

    @Test
    void deveRetornar404ParaIdInexistente() throws Exception {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/livros/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoLivroSemAutor() throws Exception {
        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Dom Casmurro", null)))
                .andExpect(status().isBadRequest());

        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void deveRetornar400QuandoAutorNaoExiste() throws Exception {
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Dom Casmurro", 99L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());

        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void deveRetornar400QuandoTituloEmBranco() throws Exception {
        mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", 1L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarLivrosOrdenados() throws Exception {
        when(livroRepository.findAllByOrderByTituloAsc()).thenReturn(List.of(
                novoLivro(2L, "A Hora da Estrela", clarice),
                novoLivro(1L, "Dom Casmurro", machado)));

        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo").value("A Hora da Estrela"))
                .andExpect(jsonPath("$[1].titulo").value("Dom Casmurro"));
    }

    @Test
    void deveListarLivrosDeUmAutor() throws Exception {
        when(livroRepository.findByAutorId(1L)).thenReturn(List.of(
                novoLivro(1L, "Dom Casmurro", machado),
                novoLivro(2L, "Memorias Postumas de Bras Cubas", machado)));

        mockMvc.perform(get("/api/livros/autor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].autor.nome").value("Machado de Assis"));
    }

    @Test
    void deveAtualizarLivroInclusiveOAutor() throws Exception {
        when(livroRepository.findById(1L)).thenReturn(Optional.of(novoLivro(1L, "Dom Casmurro", machado)));
        when(autorRepository.findById(2L)).thenReturn(Optional.of(clarice));
        when(livroRepository.save(any(Livro.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/livros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("A Hora da Estrela", 2L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("A Hora da Estrela"))
                .andExpect(jsonPath("$.autor.id").value(2))
                .andExpect(jsonPath("$.autor.nome").value("Clarice Lispector"));
    }

    @Test
    void deveRetornar404AoAtualizarIdInexistente() throws Exception {
        when(livroRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/livros/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Qualquer", 1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDeletarLivro() throws Exception {
        when(livroRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/livros/1"))
                .andExpect(status().isOk());

        verify(livroRepository).deleteById(1L);
    }

    @Test
    void deveRetornar404AoDeletarIdInexistente() throws Exception {
        when(livroRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/livros/99"))
                .andExpect(status().isNotFound());

        verify(livroRepository, never()).deleteById(any());
    }
}
