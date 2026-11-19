package com.biblioteca.controller;

import com.biblioteca.model.Autor;
import com.biblioteca.repository.AutorRepository;
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
 * Testes unitarios do AutorController: so a camada web (MockMvc),
 * com o AutorRepository substituido por um mock do Mockito.
 */
@WebMvcTest(AutorController.class)
class AutorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AutorRepository autorRepository;

    private Autor novoAutor(Long id, String nome) {
        return new Autor(id, nome, "Brasileira", LocalDate.of(1839, 6, 21), LocalDateTime.now());
    }

    @Test
    void deveCriarAutorComSucesso() throws Exception {
        Autor entrada = new Autor(null, "Machado de Assis", "Brasileira", LocalDate.of(1839, 6, 21), null);
        when(autorRepository.save(any(Autor.class))).thenReturn(novoAutor(1L, "Machado de Assis"));

        mockMvc.perform(post("/api/autores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Machado de Assis"))
                .andExpect(jsonPath("$.dataNascimento").value("1839-06-21"));
    }

    @Test
    void deveRetornar404ParaIdInexistente() throws Exception {
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/autores/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoNomeEmBranco() throws Exception {
        Autor semNome = new Autor(null, "", null, null, null);

        mockMvc.perform(post("/api/autores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semNome)))
                .andExpect(status().isBadRequest());

        verify(autorRepository, never()).save(any(Autor.class));
    }

    @Test
    void deveListarAutoresOrdenados() throws Exception {
        when(autorRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(
                novoAutor(2L, "Clarice Lispector"),
                novoAutor(1L, "Machado de Assis")));

        mockMvc.perform(get("/api/autores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Clarice Lispector"))
                .andExpect(jsonPath("$[1].nome").value("Machado de Assis"));
    }

    @Test
    void deveBuscarAutorPorId() throws Exception {
        when(autorRepository.findById(1L)).thenReturn(Optional.of(novoAutor(1L, "Machado de Assis")));

        mockMvc.perform(get("/api/autores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Machado de Assis"));
    }

    @Test
    void deveAtualizarAutor() throws Exception {
        when(autorRepository.findById(1L)).thenReturn(Optional.of(novoAutor(1L, "Machado")));
        when(autorRepository.save(any(Autor.class))).thenAnswer(inv -> inv.getArgument(0));
        Autor dados = new Autor(null, "Machado de Assis", "Brasileira", LocalDate.of(1839, 6, 21), null);

        mockMvc.perform(put("/api/autores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dados)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Machado de Assis"));
    }

    @Test
    void deveRetornar404AoAtualizarIdInexistente() throws Exception {
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());
        Autor dados = new Autor(null, "Qualquer", null, null, null);

        mockMvc.perform(put("/api/autores/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dados)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDeletarAutor() throws Exception {
        when(autorRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/autores/1"))
                .andExpect(status().isOk());

        verify(autorRepository).deleteById(1L);
    }

    @Test
    void deveRetornar404AoDeletarIdInexistente() throws Exception {
        when(autorRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/autores/99"))
                .andExpect(status().isNotFound());

        verify(autorRepository, never()).deleteById(any());
    }
}
