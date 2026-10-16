package controllers;

import views.ConsoleView;

public class MenuController {

    private final ConsoleView view;
    private final JogoController jogoCtrl;

    public MenuController(ConsoleView view, JogoController jogoCtrl) {
        this.view = view;
        this.jogoCtrl = jogoCtrl;
    }

    public void processarMenu() {
        boolean executando = true;

        while (executando) {
            view.limparTela();
            view.exibirMensagem("==========================================");
            view.exibirMensagem("       JOGO NARRATIVO INTERATIVO          ");
            view.exibirMensagem("==========================================");
            view.exibirMensagem("[1] Nova Partida");
            view.exibirMensagem("[2] Carregar / Excluir Saves (3 Slots)");
            view.exibirMensagem("[3] Galeria de Conquistas e Finais");
            view.exibirMensagem("[4] Preferências do Usuário");
            view.exibirMensagem("[5] Instruções e Créditos");
            view.exibirMensagem("[0] Sair");
            view.exibirMensagem("==========================================");

            switch (view.lerEntrada().trim()) {
                case "1":
                    view.limparTela();
                    view.exibirMensagem("Digite o nome do seu Protagonista:");
                    String nome = view.lerEntrada().trim();
                    jogoCtrl.iniciarNovoJogo(nome.isEmpty() ? "Investigador" : nome);
                    break;
                case "2":
                    jogoCtrl.iniciarPartida(true);
                    break;
                case "3":
                    jogoCtrl.exibirGaleriaEFinais();
                    break;
                case "4":
                    jogoCtrl.gerenciarMenuPreferencias();
                    break;
                case "5":
                    view.limparTela();
                    view.exibirMensagem("--- INSTRUÇÕES E CRÉDITOS ---");
                    view.exibirMensagem("Suas escolhas moldam a narrativa. Use os 3 slots para testar diferentes rotas.");
                    view.exibirMensagem("Projeto EXA863 - MI de Programação (UEFS).");
                    view.pausarParaLeitura();
                    break;
                case "0":
                    executando = false;
                    break;
                default:
                    view.exibirMensagem("Opção inválida!");
                    view.pausarParaLeitura();
            }
        }
    }
}