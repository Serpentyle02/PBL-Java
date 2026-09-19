public class MenuController {
    private ConsoleView view;
    private JogoController jogoCtrl;

    public MenuController(ConsoleView view, JogoController jogoCtrl) {
        this.view = view; 
        this.jogoCtrl = jogoCtrl;
    }

    public void processarMenu() {
        boolean ativo = true;
        while (ativo) {
            int op = view.exibirMenuInicial();
            switch (op) {
                case 1 -> {
                    String nome = view.solicitarNomeJogador();
                    jogoCtrl.iniciarNovoJogo(nome);
                    // O jogo roda e, ao finalizar, retorna aqui para continuar no menu
                }
                case 2 -> {
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
                    ativo = false; // Encerra o loop e finaliza o programa
                }
                default -> view.exibirMensagem("\nOpção inválida! Escolha um número do menu.");
            }
        }
    }
}