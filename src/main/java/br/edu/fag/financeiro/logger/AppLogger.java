package br.edu.fag.financeiro.logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger no padrao Singleton CLASSICO (sem anotacoes do Spring).
 *
 * - Construtor privado: ninguem fora da classe consegue dar "new AppLogger()".
 * - Inicializacao lazy: a instancia so e criada na primeira chamada a getInstance().
 * - Thread-safe: campo volatile + dupla checagem (double-checked locking).
 * - Escreve no console e no arquivo app.log (modo append).
 *
 * ATENCAO: nunca passe senha, token ou CPF completo nas mensagens.
 */
public final class AppLogger {

    private static final Path ARQUIVO = Paths.get("app.log");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // volatile garante que outras threads enxerguem a instancia ja totalmente construida
    private static volatile AppLogger instancia;

    // trava usada para que duas threads nao misturem linhas no console/arquivo
    private final Object trava = new Object();

    private AppLogger() {
    }

    public static AppLogger getInstance() {
        AppLogger resultado = instancia;              // 1a checagem (sem lock, rapida)
        if (resultado == null) {
            synchronized (AppLogger.class) {
                resultado = instancia;                // 2a checagem (com lock)
                if (resultado == null) {
                    instancia = resultado = new AppLogger();
                }
            }
        }
        return resultado;
    }

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
            // so tipo e mensagem da excecao (sem stack trace gigante e sem dados do usuario)
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
