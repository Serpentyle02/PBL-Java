public class MenuController {
    private ConsoleView view;
    private JogoController jogoCtrl;

    public MenuController(ConsoleView view, JogoController jogoCtrl) {
        this.view = view; this.jogoCtrl = jogoCtrl;
    }

    public void processarMenu() {
        boolean ativo = true;
        while(ativo) {
            int op = view.exibirMenuInicial();
            if (op == 1) {
                String nome = view.solicitarNomeJogador();
                jogoCtrl.iniciarNovoJogo(nome);
                ativo = false;
            } else {
                view.exibirMensagem("Saindo do jogo...");
                System.exit(0);
            }
        }
    }
}