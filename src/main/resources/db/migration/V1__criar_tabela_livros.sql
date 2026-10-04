-- Compatível com MySQL 8 e H2 (modo MySQL, usado nos testes).
CREATE TABLE livros (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    titulo         VARCHAR(200) NOT NULL,
    autor          VARCHAR(150) NOT NULL,
    ano_publicacao INT,
    editora        VARCHAR(150),
    disponivel     BOOLEAN      NOT NULL DEFAULT TRUE,
    imagem_url     VARCHAR(500),
    PRIMARY KEY (id)
);

CREATE INDEX idx_livros_titulo ON livros (titulo);
CREATE INDEX idx_livros_autor ON livros (autor);
