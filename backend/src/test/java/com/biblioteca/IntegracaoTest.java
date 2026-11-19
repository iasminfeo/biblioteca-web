package com.biblioteca;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integracao: sobe a aplicacao inteira (controllers + repositories + JPA)
 * usando o banco H2 em memoria do perfil "test" (application-test.properties).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Cria um autor via POST e devolve o id gerado. */
    private long criarAutor(String nome) throws Exception {
        String json = """
                {"nome": "%s", "nacionalidade": "Brasileira", "dataNascimento": "1839-06-21"}
                """.formatted(nome);

        String resposta = mockMvc.perform(post("/api/autores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode autor = objectMapper.readTree(resposta);
        return autor.get("id").asLong();
    }

    /** Cria um livro via POST ligado ao autor informado e devolve o id gerado. */
    private long criarLivro(String titulo, long autorId) throws Exception {
        String json = """
                {"titulo": "%s", "isbn": "978-8535910667", "anoPublicacao": 1899,
                 "genero": "Romance", "autor": {"id": %d}}
                """.formatted(titulo, autorId);

        String resposta = mockMvc.perform(post("/api/livros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor.id").value(autorId))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(resposta).get("id").asLong();
    }

    @Test
    void deveVincularLivroAAutor() throws Exception {
        // 1) cria o autor e pega o id
        long autorId = criarAutor("Machado de Assis");

        // 2) cria o livro com "autor": {"id": autorId}
        long livroId = criarLivro("Dom Casmurro", autorId);

        // 3) busca o livro e confere que o autor veio completo
        mockMvc.perform(get("/api/livros/" + livroId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"))
                .andExpect(jsonPath("$.genero").value("Romance"))
                .andExpect(jsonPath("$.autor.id").value(autorId))
                .andExpect(jsonPath("$.autor.nome").value("Machado de Assis"));

        // 4) lista os livros do autor
        mockMvc.perform(get("/api/livros/autor/" + autorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Dom Casmurro"));
    }

    @Test
    void deveExecutarFluxoCompletoAutor() throws Exception {
        // criar
        long id = criarAutor("Clarice");

        // buscar
        mockMvc.perform(get("/api/autores/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Clarice"))
                .andExpect(jsonPath("$.criadoEm").exists());

        // atualizar
        mockMvc.perform(put("/api/autores/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Clarice Lispector", "nacionalidade": "Brasileira", "dataNascimento": "1920-12-10"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Clarice Lispector"))
                .andExpect(jsonPath("$.dataNascimento").value("1920-12-10"));

        // deletar
        mockMvc.perform(delete("/api/autores/" + id))
                .andExpect(status().isOk());

        // depois de deletado -> 404
        mockMvc.perform(get("/api/autores/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarOAutorDoLivro() throws Exception {
        long autor1 = criarAutor("Autor Um");
        long autor2 = criarAutor("Autor Dois");
        long livroId = criarLivro("Livro Teste", autor1);

        mockMvc.perform(put("/api/livros/" + livroId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Livro Teste", "autor": {"id": %d}}
                                """.formatted(autor2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor.id").value(autor2))
                .andExpect(jsonPath("$.autor.nome").value("Autor Dois"));
    }

    @Test
    void deveRetornar409AoExcluirAutorComLivros() throws Exception {
        long autorId = criarAutor("Autor Com Livro");
        long livroId = criarLivro("Livro Vinculado", autorId);

        // a FK autor_id protege os dados: nao deixa excluir o autor
        mockMvc.perform(delete("/api/autores/" + autorId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").exists());

        // excluindo o livro antes, o autor pode ser excluido
        mockMvc.perform(delete("/api/livros/" + livroId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/autores/" + autorId))
                .andExpect(status().isOk());
    }
}
