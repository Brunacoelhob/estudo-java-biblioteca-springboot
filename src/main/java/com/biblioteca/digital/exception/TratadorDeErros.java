package com.biblioteca.digital.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte exceções em respostas JSON padronizadas (RFC 9457, "problem details").
 * Herda do tratador base do Spring para que erros de infraestrutura MVC (rota inexistente, método não
 * permitido, tipo de mídia inválido...) mantenham seu status correto em vez de virarem 500.
 */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(LivroNaoEncontradoException.class)
    ProblemDetail naoEncontrado(LivroNaoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados inválidos");
        p.setProperty("campos", campos);
        return ResponseEntity.badRequest().body(p);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requisição malformada"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail tipoInvalido(MethodArgumentTypeMismatchException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + e.getName());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception e) {
        // O detalhe vai para o log; o cliente recebe só uma mensagem genérica.
        log.error("Erro inesperado", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }
}
