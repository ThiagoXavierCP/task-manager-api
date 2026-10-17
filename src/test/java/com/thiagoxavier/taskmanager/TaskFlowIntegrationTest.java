package com.thiagoxavier.taskmanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testa o fluxo completo pela API: cadastro, login, CRUD de tarefas e isolamento entre usuários.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = registerAndGetToken("user-" + UUID.randomUUID() + "@email.com");
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void cadastroComEmailDuplicadoRetorna422() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@email.com";
        registerAndGetToken(email);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@email.com";
        registerAndGetToken(email);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"senha-errada"}
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criaListaAtualizaEConcluiTarefa() throws Exception {
        long id = createTask(token, "Estudar Spring Security");

        mvc.perform(get("/api/tasks").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Estudar Spring Security"));

        mvc.perform(patch("/api/tasks/{id}/status", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        mvc.perform(patch("/api/tasks/{id}/status", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"TODO\"}"))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/api/tasks/summary").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(1))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void filtraPorBuscaNoTitulo() throws Exception {
        createTask(token, "Revisar pull request");
        createTask(token, "Comprar café");

        mvc.perform(get("/api/tasks").param("search", "PULL")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Revisar pull request"));
    }

    @Test
    void usuarioNaoEnxergaTarefaDeOutro() throws Exception {
        long id = createTask(token, "Tarefa privada");
        String outroToken = registerAndGetToken("outro-" + UUID.randomUUID() + "@email.com");

        mvc.perform(get("/api/tasks/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(outroToken)))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/tasks/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(outroToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void validaCamposObrigatorios() throws Exception {
        mvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists());
    }

    // ---------- helpers ----------

    private String registerAndGetToken(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("accessToken").asText();
    }

    private long createTask(String token, String title) throws Exception {
        String body = mvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"%s","priority":"HIGH"}
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(body);
        return json.get("id").asLong();
    }

    private static String registerJson(String email) {
        return """
                {"name":"Usuário Teste","email":"%s","password":"senha-segura-123"}
                """.formatted(email);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
