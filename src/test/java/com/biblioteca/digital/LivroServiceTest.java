package com.biblioteca.digital;

import com.biblioteca.digital.dto.LivroRequest;
import com.biblioteca.digital.exception.LivroNaoEncontradoException;
import com.biblioteca.digital.model.Livro;
import com.biblioteca.digital.repository.LivroRepository;
import com.biblioteca.digital.service.LivroService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock LivroRepository repositorio;

    private LivroService servico() {
        return new LivroService(repositorio);
    }

    @Test
    void criar_apara_espacos_e_transforma_texto_vazio_em_nulo() {
        when(repositorio.save(any(Livro.class))).thenAnswer(i -> i.getArgument(0));

        servico().criar(new LivroRequest("  Dom Casmurro ", " Machado ", 1899, "   ", null, ""));

        ArgumentCaptor<Livro> capturado = ArgumentCaptor.forClass(Livro.class);
        verify(repositorio).save(capturado.capture());
        Livro salvo = capturado.getValue();
        assertEquals("Dom Casmurro", salvo.getTitulo());
        assertEquals("Machado", salvo.getAutor());
        assertNull(salvo.getEditora());
        assertNull(salvo.getImagemUrl());
        assertTrue(salvo.isDisponivel(), "sem informar, o livro nasce disponível");
    }

    @Test
    void atualizar_inexistente_lanca_excecao_de_dominio() {
        when(repositorio.findById(5L)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class,
                () -> servico().atualizar(5L, new LivroRequest("A", "B", null, null, true, null)));
        verify(repositorio, never()).save(any());
    }

    @Test
    void remover_inexistente_lanca_excecao_e_nao_deleta() {
        when(repositorio.findById(5L)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class, () -> servico().remover(5L));
        verify(repositorio, never()).delete(any());
    }

    @Test
    void listar_sem_busca_nao_usa_a_consulta_de_filtro() {
        servico().listar("   ");

        verify(repositorio).findAll();
        verify(repositorio, never()).buscar(any());
    }
}
