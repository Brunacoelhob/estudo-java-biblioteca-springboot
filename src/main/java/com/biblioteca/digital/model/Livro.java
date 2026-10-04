package com.biblioteca.digital.model;

import jakarta.persistence.*;

/** Entidade persistida. Nunca é exposta diretamente pela API (veja os DTOs). */
@Entity
@Table(name = "livros")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    private Integer anoPublicacao;

    @Column(length = 150)
    private String editora;

    @Column(nullable = false)
    private boolean disponivel = true;

    @Column(length = 500)
    private String imagemUrl;

    protected Livro() {
        // exigido pelo JPA
    }

    public Livro(String titulo, String autor, Integer anoPublicacao, String editora, boolean disponivel, String imagemUrl) {
        atualizar(titulo, autor, anoPublicacao, editora, disponivel, imagemUrl);
    }

    /** Único ponto de alteração dos dados: mantém o objeto consistente. */
    public void atualizar(String titulo, String autor, Integer anoPublicacao, String editora, boolean disponivel, String imagemUrl) {
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.editora = editora;
        this.disponivel = disponivel;
        this.imagemUrl = imagemUrl;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public Integer getAnoPublicacao() { return anoPublicacao; }
    public String getEditora() { return editora; }
    public boolean isDisponivel() { return disponivel; }
    public String getImagemUrl() { return imagemUrl; }
}
