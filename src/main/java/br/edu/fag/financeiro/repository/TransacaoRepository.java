package br.edu.fag.financeiro.repository;

import br.edu.fag.financeiro.model.TipoTransacao;
import br.edu.fag.financeiro.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query("select coalesce(sum(t.valor), 0) from Transacao t where t.tipo = :tipo")
    BigDecimal somarPorTipo(@Param("tipo") TipoTransacao tipo);
}
