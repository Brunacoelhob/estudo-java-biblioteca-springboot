package com.biblioteca.digital.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados aceitos para criar/atualizar um livro. Só estes campos entram: o cliente não controla o id. */
public record LivroRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
        String titulo,

        @NotBlank(message = "O autor é obrigatório")
        @Size(max = 150, message = "O autor deve ter no máximo 150 caracteres")
        String autor,

        @Min(value = 1, message = "O ano deve ser positivo")
        @Max(value = 2100, message = "O ano é inválido")
        Integer anoPublicacao,

        @Size(max = 150, message = "A editora deve ter no máximo 150 caracteres")
        String editora,

        Boolean disponivel,

        // Só http(s): bloqueia esquemas perigosos como javascript: e data:.
        @Size(max = 500, message = "A URL da imagem deve ter no máximo 500 caracteres")
        @Pattern(regexp = "^$|^https?://.+", message = "A imagem deve ser uma URL http(s)")
        String imagemUrl
) {
    public boolean disponivelOuPadrao() {
        return disponivel == null || disponivel;
    }
}
