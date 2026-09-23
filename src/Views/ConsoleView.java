import java.util.Scanner;
import java.util.List;

/**
 * Componente View da arquitetura MVC.
 * Isola chamadas de sistema, limpezas de tela e leituras do Console.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class ConsoleView {
    private Scanner scanner; // Leitor do fluxo de entrada do sistema

    /**
     * Construtor da View.
     */
    public ConsoleView() { 
        this.scanner = new Scanner(System.in); // Inicializa o Scanner no System.in
    }

    /**
     * Limpa o terminal identificando o Sistema Operacional ou utilizando fallback de linhas.
     */
    public void limparTela() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                // Executa comando 'cls' no CMD do Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Aplica sequências de escape ANSI para Linux/macOS
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Imprime linhas em branco se o terminal for restrito
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Renderiza o menu inicial e capta a opção do usuário.
     */
    public int exibirMenuInicial() {
        System.out.println("\n=== O ROUBO QUÂNTICO ===");
        System.out.println("1. Nova Partida");
        System.out.println("2. Créditos");
        System.out.println("3. Sair");
        System.out.print("Escolha: ");
        try {
            return Integer.parseInt(scanner.nextLine()); // Retorna opção numérica
        } catch (NumberFormatException e) {
            return -1; // Retorna código de erro para opção inválida
        }
    }

    /**
     * Solicita o nome do jogador.
     */
    public String solicitarNomeJogador() {
        System.out.print("Informe o nome do seu detetive acadêmico: ");
        return scanner.nextLine(); // Captura a linha digitada
    }

    /**
     * Exibe o texto descritivo da cena.
     */
    public void exibirTextoCena(String txt) { 
        System.out.println(txt); // Imprime a narrativa
    }

    /**
     * Exibe a lista de opções numeradas e captura a escolha.
     */
    public int exibirOpcoes(List<Opcao> opt) {
        System.out.println();
        for (int i = 0; i < opt.size(); i++) {
            System.out.println((i + 1) + ". " + opt.get(i).getTexto()); // Exibe opção formatada
        }
        System.out.print("Sua escolha: ");
        return Integer.parseInt(scanner.nextLine()) - 1; // Retorna o índice correspondente
    }

    /**
     * Exibe mensagens gerais de sistema.
     */
    public void exibirMensagem(String msg) { 
        System.out.println(msg); // Imprime a mensagem genérica
    }
}