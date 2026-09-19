import java.util.Scanner;
import java.util.List;

public class ConsoleView {
    private Scanner scanner;

    public ConsoleView() { 
        this.scanner = new Scanner(System.in); 
    }

    public void limparTela() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    public int exibirMenuInicial() {
        System.out.println("\n=== O ROUBO QUÂNTICO ===");
        System.out.println("1. Nova Partida");
        System.out.println("2. Créditos");
        System.out.println("3. Sair");
        System.out.print("Escolha: ");
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String solicitarNomeJogador() {
        System.out.print("Informe o nome do seu detetive acadêmico: ");
        return scanner.nextLine();
    }

    public void exibirTextoCena(String txt) { 
        System.out.println(txt); 
    }

    public int exibirOpcoes(List<Opcao> opt) {
        System.out.println();
        for (int i = 0; i < opt.size(); i++) {
            System.out.println((i + 1) + ". " + opt.get(i).getTexto());
        }
        System.out.print("Sua escolha: ");
        return Integer.parseInt(scanner.nextLine()) - 1;
    }

    public void exibirMensagem(String msg) { 
        System.out.println(msg); 
    }
}