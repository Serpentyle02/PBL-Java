package views;

import models.IdiomaManager;
import models.Protagonista;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Camada de visualização (View) do padrão MVC.
 * Responsável exclusivamente pela interação com o console: exibição de mensagens,
 * efeito de digitação da narrativa, barra de status do protagonista, menus traduzidos,
 * leitura de entradas do teclado e limpeza completa da tela do terminal.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class ConsoleView {

    // Leitor único da entrada padrão do sistema (teclado)
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Imprime uma linha de texto simples no console.
     *
     * @param msg Mensagem a ser exibida.
     */
    public void exibirMensagem(String msg) {
        System.out.println(msg);
    }

    /**
     * Exibe o texto narrativo da cena respeitando a preferência de velocidade do jogador.
     * Se {@code velocidadeMs <= 0}, imprime o texto instantaneamente; caso contrário,
     * imprime caractere por caractere com pausa em milissegundos (efeito máquina de escrever).
     *
     * @param texto        Texto narrativo da cena.
     * @param velocidadeMs Intervalo em milissegundos entre cada caractere.
     */
    public void exibirNarrativaComPreferencia(String texto, int velocidadeMs) {
        if (texto == null) return;
        if (velocidadeMs <= 0) {
            System.out.println(texto);
            return;
        }
        for (int i = 0; i < texto.length(); i++) {
            System.out.print(texto.charAt(i));
            System.out.flush();
            try {
                Thread.sleep(velocidadeMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.print(texto.substring(i + 1));
                break;
            }
        }
        System.out.println();
    }

    /**
     * Exibe o menu principal no idioma atual e lê a opção numérica escolhida pelo jogador.
     *
     * @param mgr Gerenciador de idioma ativo.
     * @return Número inteiro correspondente à opção escolhida, ou -1 se a entrada não for numérica.
     */
    public int exibirMenuInicial(IdiomaManager mgr) {
        System.out.println(mgr.get("MENU_TITULO"));
        System.out.println(mgr.get("MENU_OP1"));
        System.out.println(mgr.get("MENU_OP2"));
        System.out.println(mgr.get("MENU_OP3"));
        System.out.println(mgr.get("MENU_OP4"));
        System.out.println(mgr.get("MENU_OP5"));
        System.out.println(mgr.get("MENU_OP6"));
        System.out.println(mgr.get("MENU_OP0"));
        try {
            return Integer.parseInt(lerEntrada().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Exibe o menu de seleção de idioma (Português, Inglês ou Espanhol).
     *
     * @return Código do idioma selecionado ({@code "pt_BR"}, {@code "en_US"} ou {@code "es_ES"}).
     */
    public String exibirMenuIdiomas() {
        return exibirMenuIdiomas("pt_BR");
    }

    /**
     * Limpa a tela, exibe o menu de seleção de idioma e devolve o código escolhido.
     * Caso o usuário digite uma opção fora de 1..3, mantém o idioma atual.
     *
     * @param idiomaAtual Código do idioma ativo antes da seleção.
     * @return Código do idioma selecionado ({@code "pt_BR"}, {@code "en_US"} ou {@code "es_ES"}).
     */
    public String exibirMenuIdiomas(String idiomaAtual) {
        limparTela();
        System.out.println("=== SELECT LANGUAGE / SELEÇÃO DE IDIOMA ===");
        System.out.println("[1] Português (Brasil)");
        System.out.println("[2] English (US)");
        System.out.println("[3] Español");
        String op = lerEntrada().trim();
        switch (op) {
            case "1": return "pt_BR";
            case "2": return "en_US";
            case "3": return "es_ES";
            default:  return (idiomaAtual != null && !idiomaAtual.isEmpty()) ? idiomaAtual : "pt_BR";
        }
    }

    /**
     * Solicita o nome do protagonista no idioma atual e retorna o texto digitado.
     *
     * @param mgr Gerenciador de idioma ativo.
     * @return Nome informado pelo jogador.
     */
    public String solicitarNomeJogador(IdiomaManager mgr) {
        System.out.print(mgr.get("SOLICITAR_NOME"));
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            encerrarPorFimDeEntrada();
            return "Investigador";
        }
    }

    /**
     * Renderiza o cabeçalho superior com o nome do jogador, todos os seus atributos atuais
     * (Lógica, Carisma, Estresse, Coragem, Pistas e Reputação) e o mapa de relacionamentos
     * com NPCs no idioma configurado.
     *
     * @param p   Instância do Protagonista em jogo.
     * @param mgr Gerenciador de idioma ativo.
     */
    public void exibirStatusProtagonista(Protagonista p, IdiomaManager mgr) {
        if (p == null) return;
        System.out.println("==================================================================");
        System.out.println(" " + p.getNome()
                + " | " + mgr.get("STATUS_LOG") + p.getLogica()
                + mgr.get("STATUS_CAR") + p.getCarisma()
                + mgr.get("STATUS_EST") + p.getEstresse()
                + mgr.get("STATUS_COR") + p.getCoragem()
                + " | " + mgr.get("STATUS_PIS") + p.getPistas()
                + mgr.get("STATUS_REP") + p.getReputacao());

        if (!p.getRelacionamentos().isEmpty()) {
            System.out.println(mgr.get("STATUS_REL") + p.getRelacionamentos());
        }
        System.out.println("==================================================================");
    }

    /**
     * Sobrecarga que renderiza o cabeçalho superior do protagonista no formato padrão.
     *
     * @param p Instância do Protagonista em jogo.
     */
    public void exibirStatusProtagonista(Protagonista p) {
        if (p == null) return;
        System.out.println("==================================================================");
        System.out.printf(" %s | Lóg: %d | Car: %d | Est: %d | Cor: %d | Pistas: %d | Rep: %d%n",
                p.getNome(), p.getLogica(), p.getCarisma(), p.getEstresse(),
                p.getCoragem(), p.getPistas(), p.getReputacao());
        if (!p.getRelacionamentos().isEmpty()) {
            System.out.println(" Relações: " + p.getRelacionamentos());
        }
        System.out.println("==================================================================");
    }

    /**
     * Lê uma linha digitada pelo usuário no terminal.
     * Caso o fluxo de entrada seja encerrado (EOF / Ctrl+Z / Ctrl+D), finaliza o programa de forma limpa.
     *
     * @return String digitada pelo usuário.
     */
    public String lerEntrada() {
        System.out.print("> ");
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            encerrarPorFimDeEntrada();
            return "0";
        }
    }

    /**
     * Pausa a execução exibindo a mensagem no idioma atual e aguarda o usuário pressionar ENTER.
     *
     * @param mgr Gerenciador de idioma ativo.
     */
    public void pausarParaLeitura(IdiomaManager mgr) {
        System.out.println(mgr.get("PAUSA_ENTER"));
        try {
            scanner.nextLine();
        } catch (NoSuchElementException e) {
            encerrarPorFimDeEntrada();
        }
    }

    /**
     * Pausa a execução e aguarda o usuário pressionar ENTER para prosseguir a leitura.
     */
    public void pausarParaLeitura() {
        System.out.println("\n[Pressione ENTER para continuar...]");
        try {
            scanner.nextLine();
        } catch (NoSuchElementException e) {
            encerrarPorFimDeEntrada();
        }
    }

    /**
     * Encerra a aplicação de maneira segura caso a entrada padrão seja fechada inesperadamente.
     */
    private void encerrarPorFimDeEntrada() {
        System.out.println("\n[Entrada encerrada. Saindo do jogo.]");
        System.exit(0);
    }

    /**
     * Apaga o terminal por completo (inclusive o histórico de rolagem), exibindo apenas a tela atual.
     * No Windows executa o comando nativo {@code cls}; nos demais sistemas utiliza sequências ANSI.
     */
    public void limparTela() {
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                return;
            }
        } catch (IOException e) {
            // Cai para o método ANSI abaixo caso o comando cls falhe
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }
}
