package com.biblioteca.digital.service;

import com.biblioteca.digital.dto.LivroRequest;
import com.biblioteca.digital.dto.LivroResponse;
import com.biblioteca.digital.exception.LivroNaoEncontradoException;
import com.biblioteca.digital.model.Livro;
import com.biblioteca.digital.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LivroService {

    private final LivroRepository repositorio;

    public LivroService(LivroRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<LivroResponse> listar(String busca) {
        List<Livro> livros = busca == null || busca.isBlank()
                ? repositorio.findAll()
                : repositorio.buscar(busca.trim());
        return livros.stream().map(LivroResponse::de).toList();
    }

    public LivroResponse buscarPorId(Long id) {
        return LivroResponse.de(obter(id));
    }

    @Transactional
    public LivroResponse criar(LivroRequest r) {
        Livro livro = new Livro(r.titulo().trim(), r.autor().trim(), r.anoPublicacao(),
                limpar(r.editora()), r.disponivelOuPadrao(), limpar(r.imagemUrl()));
        return LivroResponse.de(repositorio.save(livro));
    }

    @Transactional
    public LivroResponse atualizar(Long id, LivroRequest r) {
        Livro livro = obter(id);
        livro.atualizar(r.titulo().trim(), r.autor().trim(), r.anoPublicacao(),
                limpar(r.editora()), r.disponivelOuPadrao(), limpar(r.imagemUrl()));
        return LivroResponse.de(livro);
    }

    @Transactional
    public void remover(Long id) {
        repositorio.delete(obter(id));
    }

    private Livro obter(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new LivroNaoEncontradoException(id));
    }

    private static String limpar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
