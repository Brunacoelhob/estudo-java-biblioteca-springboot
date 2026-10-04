package com.biblioteca.digital;

import com.biblioteca.digital.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Teste de ponta a ponta da API: HTTP → controller → serviço → JPA → banco (H2 com as migrações reais). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LivroApiTest {

    @Autowired MockMvc mvc;
    @Autowired LivroRepository repositorio;

    @BeforeEach
    void limpar() {
        repositorio.deleteAll();
    }

    private static final String VALIDO = """
            {"titulo":"O Hobbit","autor":"J.R.R. Tolkien","anoPublicacao":1937,"editora":"HarperCollins","imagemUrl":"https://exemplo.com/capa.jpg"}""";

    private long criar(String json) throws Exception {
        MvcResult r = mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/livros/")))
                .andReturn();
        String corpo = r.getResponse().getContentAsString();
        return Long.parseLong(corpo.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void ciclo_completo_criar_consultar_atualizar_remover() throws Exception {
        long id = criar(VALIDO);

        mvc.perform(get("/livros/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("O Hobbit"))
                .andExpect(jsonPath("$.disponivel").value(true));

        mvc.perform(put("/livros/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo":"O Hobbit (2ª ed.)","autor":"J.R.R. Tolkien","anoPublicacao":1937,"disponivel":false,"imagemUrl":"https://exemplo.com/nova.jpg"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponivel").value(false))
                // O original esquecia de atualizar a imagem no PUT; agora ela muda.
                .andExpect(jsonPath("$.imagemUrl").value("https://exemplo.com/nova.jpg"));

        mvc.perform(delete("/livros/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/livros/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void remover_inexistente_devolve_404_e_nao_500() throws Exception {
        mvc.perform(delete("/livros/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("9999")));
    }

    @Test
    void atualizar_inexistente_devolve_404() throws Exception {
        mvc.perform(put("/livros/9999").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void valida_campos_obrigatorios_e_informa_quais() throws Exception {
        mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo":"  ","autor":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists())
                .andExpect(jsonPath("$.campos.autor").exists());
    }

    @Test
    void rejeita_url_de_imagem_com_esquema_perigoso() throws Exception {
        mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content("""
                        {"titulo":"X","autor":"Y","imagemUrl":"javascript:alert(1)"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.imagemUrl").exists());
    }

    @Test
    void rejeita_ano_invalido_e_json_malformado() throws Exception {
        mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"X\",\"autor\":\"Y\",\"anoPublicacao\":-5}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content("{nao é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Requisição malformada"));
    }

    @Test
    void cliente_nao_consegue_impor_o_id() throws Exception {
        long id = criar("{\"id\":777,\"titulo\":\"X\",\"autor\":\"Y\"}");
        org.junit.jupiter.api.Assertions.assertNotEquals(777L, id);
    }

    @Test
    void busca_por_titulo_ou_autor_sem_diferenciar_maiusculas() throws Exception {
        criar(VALIDO);
        criar("{\"titulo\":\"Dom Casmurro\",\"autor\":\"Machado de Assis\"}");

        mvc.perform(get("/livros").param("busca", "tolkien"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("O Hobbit"));
        mvc.perform(get("/livros").param("busca", "CASMURRO"))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/livros")).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void parametro_de_busca_com_curinga_nao_causa_erro() throws Exception {
        criar(VALIDO);
        mvc.perform(get("/livros").param("busca", "'; DROP TABLE livros; --"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/livros")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void id_nao_numerico_devolve_400() throws Exception {
        mvc.perform(get("/livros/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void cors_so_para_origens_configuradas() throws Exception {
        mvc.perform(options("/livros").header("Origin", "http://localhost:8081")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
        mvc.perform(options("/livros").header("Origin", "http://site-malicioso.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void saude_esta_disponivel_e_detalhes_internos_nao_sao_expostos() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
    }
}
