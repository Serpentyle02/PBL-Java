import java.util.Scanner;

public class PBL {
    private Scanner scanner;

    public PBL() {
        this.scanner = new Scanner(System.in);
    }

    // ==========================================
    // 1. MENU INICIAL
    // ==========================================
    public int exibirMenuInicial() {
        System.out.println("=====================================================");
        System.out.println("            PROJETO OMEGA: MISTÉRIO NO CAMPUS        ");
        System.out.println("=====================================================");
        System.out.println("1. Nova Partida");
        System.out.println("2. Instruções");
        System.out.println("3. Créditos");
        System.out.println("4. Sair");
        System.out.println("=====================================================");
        System.out.print("Escolha uma opção: ");
        
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1; // Retorna opção inválida caso o usuário digite letras
        }
    }

    // ==========================================
    // 2. MENU DE CARACTERÍSTICAS
    // ==========================================
    public void exibirMenuCaracteristicas() {
        System.out.println("\n=====================================================");
        System.out.println("             REGISTRO DE NOVO ESTUDANTE              ");
        System.out.println("=====================================================");
        
        System.out.print("Informe seu nome de registro: ");
        String nome = scanner.nextLine();


        // Feedback direto da criação do personagem
        System.out.println("\n=====================================================");
        System.out.println("PROTAGONISTA REGISTRADO COM SUCESSO!");
        System.out.println("Nome: " + nome);
    }

    // ==========================================
    // CONTROLADOR DOS MENUS
    // ==========================================
    public void iniciar() {
        boolean rodando = true;
        while (rodando) {
            int opcao = exibirMenuInicial();
            
            switch (opcao) {
                case 1:
                    exibirMenuCaracteristicas();
                    rodando = false; // Encerra o loop do menu inicial para começar o jogo de fato
                    break;
                case 4:
                    System.out.println("\nSaindo do sistema. Até a próxima!");
                    rodando = false;
                    break;
                default:
                    System.out.println("\nOpção inválida! Tente novamente.\n");
            }
        }
        scanner.close();
    }

    // ==========================================
    // MÉTODO MAIN PARA TESTE RÁPIDO
    // ==========================================
    public static void main(String[] args) {
        PBL menu = new PBÇ();
        menu.iniciar();
    }
}
