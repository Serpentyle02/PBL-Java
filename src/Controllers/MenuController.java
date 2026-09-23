/**
 * Controller responsável pelo gerenciamento dos Menus.
 * Gerencia o fluxo inicial, tela de créditos e saída.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class MenuController {
    private ConsoleView view; // Referência da View
    private JogoController jogoCtrl; // Referência do Controller de jogo

    /**
     * Construtor do MenuController.
     */
    public MenuController(ConsoleView view, JogoController jogoCtrl) {
        this.view = view; 
        this.jogoCtrl = jogoCtrl;
    }

    /**
     * Mantém o loop ativo do menu exibindo as opções.
     */
    public void processarMenu() {
        boolean ativo = true; // Controla a execução do menu
        while (ativo) {
            int op = view.exibirMenuInicial(); // Exibe as opções e lê resposta
            switch (op) {
                case 1 -> {
                    String nome = view.solicitarNomeJogador(); // Solicita o nome na View
                    jogoCtrl.iniciarNovoJogo(nome); // Dispara o jogo
                    // Ao finalizar ou pausar, o loop continua e reexibe o menu
                }
                case 2 -> {
                    // Exibe a tela de créditos e detalhes acadêmicos do projeto
                    view.exibirMensagem("\n========================================================");
                    view.exibirMensagem("                   CRÉDITOS DO JOGO                     ");
                    view.exibirMensagem("========================================================");
                    view.exibirMensagem("Título: O Roubo Quântico");
                    view.exibirMensagem("Disciplina: EXA863 - MI - Programação (UEFS)");
                    view.exibirMensagem("Desenvolvimento: Renan Queiroz & Felipe Pereira");
                    view.exibirMensagem("Arquitetura: Padrão MVC (Model-View-Controller)");
                    view.exibirMensagem("Linguagem: Java");
                    view.exibirMensagem("========================================================\n");
                }
                case 3 -> {
                    view.exibirMensagem("Saindo do jogo... Até logo!");
                    ativo = false; // Finaliza o loop e encerra a aplicação
                }
                default -> view.exibirMensagem("\nOpção inválida! Escolha um número do menu.");
            }
        }
    }
}