# Sistema de Controle Financeiro (Spring Boot + Logger Singleton)

API REST de **despesas, receitas e alertas de gasto**. Trabalho bimestral: padrao **Singleton** aplicado a um logger (`AppLogger`).

- Java 21 · Spring Boot 3.3.5 · Spring Data JPA · H2 (em memoria) · Maven

## Como rodar no IntelliJ
1. `File > Open` e selecione a pasta `controle-financeiro` (o IntelliJ le o `pom.xml` e baixa as dependencias).
2. Confira o JDK 21 em `File > Project Structure > Project SDK`.
3. Rode `ControleFinanceiroApplication` (botao verde). A API sobe em `http://localhost:8080`.
4. Abra `requests.http` e execute as requisicoes (ou use Postman/curl).
5. Testes: `mvn test` (ou botao direito em `AppLoggerTest > Run`).
6. O console H2 fica em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:financeiro`, user `sa`, senha vazia).

O arquivo `app.log` e criado/atualizado na **raiz do projeto**. O `app.log` incluido e um exemplo.

## Rotas (recurso: `Transacao`)
| Metodo | Rota | Retorno |
|---|---|---|
| POST | `/transacoes` | 201 Created (+ header Location) |
| GET | `/transacoes` | 200 OK (lista) |
| GET | `/transacoes/{id}` | 200 OK ou 404 |
| PUT | `/transacoes/{id}` | 200 OK ou 404 |
| DELETE | `/transacoes/{id}` | 204 No Content ou 404 |
| GET | `/transacoes/resumo` | 200 OK (extra: receitas, despesas, saldo e `alertaGasto`) |

Corpo de exemplo:
```json
{ "descricao": "Aluguel", "valor": 1200.00, "tipo": "DESPESA", "categoria": "Moradia" }
```
`tipo` = `RECEITA` ou `DESPESA`. Dados invalidos retornam 400.

**Alerta de gasto:** despesa acima de `financeiro.limite-alerta-despesa` (500.00 em `application.properties`) gera uma linha `ALERTA DE GASTO` no log; `/transacoes/resumo` retorna `alertaGasto=true` quando despesas > receitas.

## Estrutura
```
logger/AppLogger.java            Singleton classico (sem Spring)  <- VERSAO FINAL
logger/Nivel.java                DEBUG, INFO, ERRO
logger/spring/AppLoggerSpring    Copia como @Component (so ativa com o profile "spring-logger")
model/Transacao, TipoTransacao, ResumoFinanceiro
repository/TransacaoRepository  (JPA)
service/TransacaoService         (regras + logs)
controller/TransacaoController, GlobalExceptionHandler
```

## Niveis de log usados
- **DEBUG**: detalhes da operacao (ex.: "Criando transacao: tipo=..., valor=...").
- **INFO**: operacao concluida (ex.: "Transacao criada com sucesso: id=1") e alertas de gasto.
- **ERRO**: recurso nao encontrado (404), validacao (400) e excecoes (500).
- **Dados sensiveis:** nunca sao registrados. O corpo da requisicao nao e logado; so campos de negocio (tipo, categoria, valor) e ids. Excecoes registram apenas tipo e mensagem.

## Como trocar de versao (opcional)
Para usar a versao Spring no lugar da classica, no `TransacaoService`:
1. Troque o campo `private final AppLogger log = AppLogger.getInstance();` por `private final AppLoggerSpring log;`
2. Receba-o no construtor: `public TransacaoService(TransacaoRepository repository, AppLoggerSpring log, @Value(...) BigDecimal limite)` e atribua `this.log = log;`
3. Rode com `--spring.profiles.active=spring-logger` (IntelliJ: *Run Configuration > Active profiles*).

---

## Respostas (Parte 3)

**Quem garante a instancia unica em cada versao?**
Na versao classica, a propria classe garante: construtor privado (ninguem faz `new`), campo estatico `volatile` e `getInstance()` com dupla checagem dentro de `synchronized`. Na versao `@Component`, quem garante e o container do Spring: o escopo padrao dos beans e *singleton*, entao ele cria um unico objeto e injeta o mesmo em quem pedir. A classe em si nao impede `new`.

**Quando o Singleton classico ainda faz sentido?**
Quando o objeto precisa existir fora do container do Spring ou antes dele subir (metodo `main`, classes utilitarias estaticas, codigo de bibliotecas, projetos sem Spring). Tambem faz sentido para ensinar/aplicar o padrao e quando se quer garantir a unicidade pela propria classe, independente de framework.

### Justificativa da versao final
A versao final usa o **Singleton classico (`AppLogger`)**. Motivos: (1) o logger e usado ate no `main`, antes de o contexto do Spring existir, onde um bean ainda nao pode ser injetado; (2) o mesmo `getInstance()` serve ao Service, ao Controller e ao `GlobalExceptionHandler` sem acoplar o logger ao framework; (3) a unicidade fica garantida pela propria classe, comprovada no `AppLoggerTest` (construtor privado, mesma instancia, 50 threads simultaneas). Em contrapartida, o `@Component` e mais facil de mockar em testes e de configurar por injecao, e seria a escolha natural se o projeto crescesse e o logger passasse a depender de configuracoes do Spring.
