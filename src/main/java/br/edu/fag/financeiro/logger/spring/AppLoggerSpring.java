package br.edu.fag.financeiro.logger.spring;

import br.edu.fag.financeiro.logger.Nivel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * COPIA do logger como @Component do Spring (parte 3 da atividade).
 *
 * Aqui NAO ha construtor privado, getInstance() nem volatile/dupla checagem:
 * quem garante a instancia unica e o container do Spring (escopo "singleton" e o padrao).
 *
 * Fica desligada por padrao (@Profile) para a aplicacao usar UMA so versao.
 * Para testar: rode com --spring.profiles.active=spring-logger e veja README.md
 * ("Como trocar de versao").
 */
@Component
@Profile("spring-logger")
public class AppLoggerSpring {

    private static final Path ARQUIVO = Paths.get("app.log");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Object trava = new Object();

    public void debug(Class<?> origem, String mensagem) {
        registrar(Nivel.DEBUG, origem, mensagem, null);
    }

    public void info(Class<?> origem, String mensagem) {
        registrar(Nivel.INFO, origem, mensagem, null);
    }

    public void erro(Class<?> origem, String mensagem) {
        registrar(Nivel.ERRO, origem, mensagem, null);
    }

    public void erro(Class<?> origem, String mensagem, Throwable causa) {
        registrar(Nivel.ERRO, origem, mensagem, causa);
    }

    private void registrar(Nivel nivel, Class<?> origem, String mensagem, Throwable causa) {
        StringBuilder linha = new StringBuilder()
                .append(LocalDateTime.now().format(FORMATO_DATA))
                .append(" [").append(nivel).append("] ")
                .append("[").append(Thread.currentThread().getName()).append("] ")
                .append(origem.getSimpleName()).append(" - ")
                .append(mensagem);

        if (causa != null) {
            linha.append(" | causa: ").append(causa.getClass().getSimpleName())
                    .append(": ").append(causa.getMessage());
        }

        synchronized (trava) {
            if (nivel == Nivel.ERRO) {
                System.err.println(linha);
            } else {
                System.out.println(linha);
            }
            try {
                Files.writeString(ARQUIVO, linha + System.lineSeparator(), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Falha ao escrever em app.log: " + e.getMessage());
            }
        }
    }
}
