package com.biblioteca.digital.exception;

public class LivroNaoEncontradoException extends RuntimeException {

    public LivroNaoEncontradoException(Long id) {
        super("Livro " + id + " não encontrado");
    }
}
