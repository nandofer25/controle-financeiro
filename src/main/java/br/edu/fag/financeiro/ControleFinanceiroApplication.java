package br.edu.fag.financeiro;

import br.edu.fag.financeiro.logger.AppLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ControleFinanceiroApplication {

    public static void main(String[] args) {
        AppLogger.getInstance().info(ControleFinanceiroApplication.class, "Iniciando Sistema de Controle Financeiro");
        SpringApplication.run(ControleFinanceiroApplication.class, args);
    }
}
