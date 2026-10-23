package controllers;

import models.IdiomaManager;
import views.ConsoleView;

/**
 * Controlador responsável pelo menu principal da aplicação e pela navegação inicial
 * entre Nova Partida, Continuar, Carregar/Excluir Saves, Galeria, Preferências, Instruções e Saída.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class MenuController {

    private final ConsoleView view;
    private final JogoController jogoCtrl;
    private final IdiomaManager idiomaManager;

    /**
     * Construtor do MenuController com gerenciador de idioma explícito.
     *
     * @param view          Instância da camada de visualização ({@link ConsoleView}).
     * @param jogoCtrl      Instância do controlador principal do jogo ({@link JogoController}).
     * @param idiomaManager Gerenciador de idioma da interface.
     */
    public MenuController(ConsoleView view, JogoController jogoCtrl, IdiomaManager idiomaManager) {
        this.view = view;
        this.jogoCtrl = jogoCtrl;
        this.idiomaManager = (idiomaManager != null) ? idiomaManager : new IdiomaManager();
    }

    /**
     * Construtor alternativo que utiliza um {@link IdiomaManager} padrão ({@code pt_BR}).
     *
     * @param view     Instância da camada de visualização ({@link ConsoleView}).
     * @param jogoCtrl Instância do controlador principal do jogo ({@link JogoController}).
     */
    public MenuController(ConsoleView view, JogoController jogoCtrl) {
        this(view, jogoCtrl, new IdiomaManager());
    }

    /**
     * Exibe o menu principal em loop até que o jogador escolha a opção [0] Sair.
     * A cada retorno de partida ou submenu, limpa o terminal para exibir apenas a tela atual.
     */
    public void processarMenu() {
        boolean executando = true;

        while (executando) {
            view.limparTela();
            String opcaoLida = String.valueOf(view.exibirMenuInicial(idiomaManager));

            switch (opcaoLida) {
                case "1":
                    view.limparTela();
                    String nome = view.solicitarNomeJogador(idiomaManager).trim();
                    jogoCtrl.iniciarNovoJogo(nome.isEmpty() ? "Investigador" : nome);
                    break;
                case "2":
                    jogoCtrl.continuarUltimaPartida();
                    break;
                case "3":
                    jogoCtrl.iniciarPartida(true);
                    break;
                case "4":
                    jogoCtrl.exibirGaleriaEFinais();
                    break;
                case "5":
                    jogoCtrl.gerenciarMenuPreferencias();
                    break;
                case "6":
                    view.limparTela();
                    view.exibirMensagem(idiomaManager.get("INSTRUCOES_TITULO"));
                    view.exibirMensagem(idiomaManager.get("INSTRUCOES_TEXTO"));
                    view.pausarParaLeitura(idiomaManager);
                    break;
                case "0":
                    view.exibirMensagem(idiomaManager.get("SAINDO"));
                    executando = false;
                    break;
                default:
                    view.exibirMensagem(idiomaManager.get("OPCAO_INVALIDA"));
                    view.pausarParaLeitura(idiomaManager);
            }
        }
    }
}
