package com.biblioteca.digital.controller;

import com.biblioteca.digital.dto.LivroRequest;
import com.biblioteca.digital.dto.LivroResponse;
import com.biblioteca.digital.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/livros")
@Tag(name = "Livros")
public class LivroController {

    private final LivroService servico;

    public LivroController(LivroService servico) {
        this.servico = servico;
    }

    @GetMapping
    @Operation(summary = "Lista os livros; use ?busca= para filtrar por título ou autor")
    public List<LivroResponse> listar(@RequestParam(required = false) String busca) {
        return servico.listar(busca);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um livro pelo id")
    public LivroResponse buscar(@PathVariable Long id) {
        return servico.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um livro")
    public ResponseEntity<LivroResponse> criar(@Valid @RequestBody LivroRequest requisicao) {
        LivroResponse criado = servico.criar(requisicao);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um livro")
    public LivroResponse atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequest requisicao) {
        return servico.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um livro")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        servico.remover(id);
        return ResponseEntity.noContent().build();
    }
}
