package br.edu.fag.financeiro.controller;

import br.edu.fag.financeiro.logger.AppLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Captura excecoes e registra ERRO no log. Nao registra o corpo da requisicao
 * (evita vazar dados sensiveis); loga apenas os campos invalidos / tipo da excecao.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AppLogger log = AppLogger.getInstance();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));

        log.erro(getClass(), "Requisicao invalida (400). Campos com erro: " + campos.keySet());
        return ResponseEntity.badRequest().body(Map.of("erro", "Dados invalidos", "campos", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException ex) {
        log.erro(getClass(), "JSON ilegivel ou valor invalido na requisicao (400)", ex);
        return ResponseEntity.badRequest().body(Map.of("erro", "JSON invalido. Confira os campos e o tipo (RECEITA ou DESPESA)."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generica(Exception ex) {
        log.erro(getClass(), "Erro inesperado (500)", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno do servidor"));
    }
}
