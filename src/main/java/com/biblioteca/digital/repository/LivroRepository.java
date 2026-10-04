package com.biblioteca.digital.repository;

import com.biblioteca.digital.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    /** Busca por trecho do título ou do autor, sem diferenciar maiúsculas/minúsculas. */
    @Query("""
            SELECT l FROM Livro l
            WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(l.autor)  LIKE LOWER(CONCAT('%', :termo, '%'))
            ORDER BY l.titulo""")
    List<Livro> buscar(@Param("termo") String termo);
}
