package br.edu.fag.financeiro.service;

import br.edu.fag.financeiro.logger.AppLogger;
import br.edu.fag.financeiro.model.ResumoFinanceiro;
import br.edu.fag.financeiro.model.TipoTransacao;
import br.edu.fag.financeiro.model.Transacao;
import br.edu.fag.financeiro.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio. O Controller so recebe e devolve; a logica fica aqui.
 *
 * VERSAO FINAL DO PROJETO: usa o AppLogger Singleton classico (ver README.md, secao "Justificativa").
 */
@Service
public class TransacaoService {

    // Singleton classico: nao e injetado, e obtido pelo getInstance()
    private final AppLogger log = AppLogger.getInstance();

    private final TransacaoRepository repository;
    private final BigDecimal limiteAlertaDespesa;

    public TransacaoService(TransacaoRepository repository,
                            @Value("${financeiro.limite-alerta-despesa:500.00}") BigDecimal limiteAlertaDespesa) {
        this.repository = repository;
        this.limiteAlertaDespesa = limiteAlertaDespesa;
    }

    public Transacao criar(Transacao transacao) {
        log.debug(getClass(), "Criando transacao: tipo=" + transacao.getTipo()
                + ", categoria=" + transacao.getCategoria() + ", valor=" + transacao.getValor());

        transacao.setId(null); // garante INSERT, mesmo que o cliente envie um id
        if (transacao.getData() == null) {
            transacao.setData(LocalDate.now());
        }

        Transacao salva = repository.save(transacao);
        verificarAlertaDeGasto(salva);

        log.info(getClass(), "Transacao criada com sucesso: id=" + salva.getId());
        return salva;
    }

    public List<Transacao> listar() {
        log.debug(getClass(), "Listando todas as transacoes");
        List<Transacao> lista = repository.findAll();
        log.info(getClass(), "Listagem concluida: " + lista.size() + " transacao(oes)");
        return lista;
    }

    public Optional<Transacao> buscarPorId(Long id) {
        log.debug(getClass(), "Buscando transacao por id=" + id);
        Optional<Transacao> encontrada = repository.findById(id);
        if (encontrada.isEmpty()) {
            log.erro(getClass(), "Transacao nao encontrada: id=" + id);
        } else {
            log.info(getClass(), "Transacao encontrada: id=" + id);
        }
        return encontrada;
    }

    public Optional<Transacao> atualizar(Long id, Transacao dados) {
        log.debug(getClass(), "Atualizando transacao id=" + id + " com tipo=" + dados.getTipo()
                + ", valor=" + dados.getValor());

        Optional<Transacao> existente = repository.findById(id);
        if (existente.isEmpty()) {
            log.erro(getClass(), "Nao foi possivel atualizar, transacao nao encontrada: id=" + id);
            return Optional.empty();
        }

        Transacao transacao = existente.get();
        transacao.setDescricao(dados.getDescricao());
        transacao.setValor(dados.getValor());
        transacao.setTipo(dados.getTipo());
        transacao.setCategoria(dados.getCategoria());
        transacao.setData(dados.getData() != null ? dados.getData() : transacao.getData());

        Transacao atualizada = repository.save(transacao);
        verificarAlertaDeGasto(atualizada);

        log.info(getClass(), "Transacao atualizada com sucesso: id=" + id);
        return Optional.of(atualizada);
    }

    public boolean excluir(Long id) {
        log.debug(getClass(), "Excluindo transacao id=" + id);
        if (!repository.existsById(id)) {
            log.erro(getClass(), "Nao foi possivel excluir, transacao nao encontrada: id=" + id);
            return false;
        }
        repository.deleteById(id);
        log.info(getClass(), "Transacao excluida com sucesso: id=" + id);
        return true;
    }

    /** Resumo de receitas x despesas. alertaGasto = true quando as despesas passam das receitas. */
    public ResumoFinanceiro resumo() {
        log.debug(getClass(), "Calculando resumo financeiro");
        BigDecimal receitas = repository.somarPorTipo(TipoTransacao.RECEITA);
        BigDecimal despesas = repository.somarPorTipo(TipoTransacao.DESPESA);
        BigDecimal saldo = receitas.subtract(despesas);
        boolean alerta = despesas.compareTo(receitas) > 0;

        if (alerta) {
            log.info(getClass(), "ALERTA DE GASTO: despesas (" + despesas + ") maiores que receitas (" + receitas + ")");
        }
        log.info(getClass(), "Resumo financeiro calculado: saldo=" + saldo);
        return new ResumoFinanceiro(receitas, despesas, saldo, alerta);
    }

    private void verificarAlertaDeGasto(Transacao t) {
        if (t.getTipo() == TipoTransacao.DESPESA && t.getValor().compareTo(limiteAlertaDespesa) > 0) {
            log.info(getClass(), "ALERTA DE GASTO: despesa id=" + t.getId() + " (" + t.getValor()
                    + ") acima do limite de " + limiteAlertaDespesa);
        }
    }
}
