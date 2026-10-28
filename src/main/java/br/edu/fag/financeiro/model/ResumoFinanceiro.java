package br.edu.fag.financeiro.model;

import java.math.BigDecimal;

/**
 * Resumo para o alerta de gasto: se as despesas passam das receitas, alertaGasto = true.
 */
public record ResumoFinanceiro(BigDecimal totalReceitas,
                               BigDecimal totalDespesas,
                               BigDecimal saldo,
                               boolean alertaGasto) {
}
