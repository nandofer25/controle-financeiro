package br.edu.fag.financeiro.controller;

import br.edu.fag.financeiro.logger.AppLogger;
import br.edu.fag.financeiro.model.ResumoFinanceiro;
import br.edu.fag.financeiro.model.Transacao;
import br.edu.fag.financeiro.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller: so recebe a requisicao, chama o Service e devolve o status HTTP.
 */
@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final AppLogger log = AppLogger.getInstance();

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    // POST /transacoes -> 201 Created
    @PostMapping
    public ResponseEntity<Transacao> criar(@Valid @RequestBody Transacao transacao) {
        log.debug(getClass(), "POST /transacoes recebido");
        Transacao salva = service.criar(transacao);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(salva.getId()).toUri();

        log.info(getClass(), "POST /transacoes -> 201 Created (id=" + salva.getId() + ")");
        return ResponseEntity.created(location).body(salva);
    }

    // GET /transacoes -> 200 OK com a lista
    @GetMapping
    public ResponseEntity<List<Transacao>> listar() {
        log.debug(getClass(), "GET /transacoes recebido");
        List<Transacao> lista = service.listar();
        log.info(getClass(), "GET /transacoes -> 200 OK (" + lista.size() + " itens)");
        return ResponseEntity.ok(lista);
    }

    // GET /transacoes/resumo -> 200 OK (receitas, despesas, saldo e alerta de gasto)
    @GetMapping("/resumo")
    public ResponseEntity<ResumoFinanceiro> resumo() {
        log.debug(getClass(), "GET /transacoes/resumo recebido");
        ResumoFinanceiro resumo = service.resumo();
        log.info(getClass(), "GET /transacoes/resumo -> 200 OK");
        return ResponseEntity.ok(resumo);
    }

    // GET /transacoes/{id} -> 200 OK ou 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<Transacao> buscarPorId(@PathVariable Long id) {
        log.debug(getClass(), "GET /transacoes/" + id + " recebido");
        return service.buscarPorId(id)
                .map(t -> {
                    log.info(getClass(), "GET /transacoes/" + id + " -> 200 OK");
                    return ResponseEntity.ok(t);
                })
                .orElseGet(() -> {
                    log.erro(getClass(), "GET /transacoes/" + id + " -> 404 Not Found");
                    return ResponseEntity.notFound().build();
                });
    }

    // PUT /transacoes/{id} -> 200 OK ou 404
    @PutMapping("/{id}")
    public ResponseEntity<Transacao> atualizar(@PathVariable Long id, @Valid @RequestBody Transacao dados) {
        log.debug(getClass(), "PUT /transacoes/" + id + " recebido");
        return service.atualizar(id, dados)
                .map(t -> {
                    log.info(getClass(), "PUT /transacoes/" + id + " -> 200 OK");
                    return ResponseEntity.ok(t);
                })
                .orElseGet(() -> {
                    log.erro(getClass(), "PUT /transacoes/" + id + " -> 404 Not Found");
                    return ResponseEntity.notFound().build();
                });
    }

    // DELETE /transacoes/{id} -> 204 No Content ou 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        log.debug(getClass(), "DELETE /transacoes/" + id + " recebido");
        if (service.excluir(id)) {
            log.info(getClass(), "DELETE /transacoes/" + id + " -> 204 No Content");
            return ResponseEntity.noContent().build();
        }
        log.erro(getClass(), "DELETE /transacoes/" + id + " -> 404 Not Found");
        return ResponseEntity.notFound().build();
    }
}
