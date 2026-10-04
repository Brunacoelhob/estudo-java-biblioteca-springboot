package com.biblioteca.digital.dto;

import com.biblioteca.digital.model.Livro;

public record LivroResponse(
        Long id,
        String titulo,
        String autor,
        Integer anoPublicacao,
        String editora,
        boolean disponivel,
        String imagemUrl
) {
    public static LivroResponse de(Livro l) {
        return new LivroResponse(l.getId(), l.getTitulo(), l.getAutor(), l.getAnoPublicacao(),
                l.getEditora(), l.isDisponivel(), l.getImagemUrl());
    }
}
