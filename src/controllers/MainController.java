package controllers;

import models.IdiomaManager;
import models.ProgressoJogo;
import views.ConsoleView;

/**
 * Classe principal (ponto de entrada da aplicação).
 * Instancia e conecta as camadas Model, View e Controller da arquitetura MVC,
 * aplica o idioma preferido salvo nos dados globais e inicia o menu principal.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class MainController {

    /**
     * Método principal que inicializa o jogo.
     *
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        SalvamentoController saveCtrl = new SalvamentoController();
        ProgressoJogo dadosGlobais = saveCtrl.carregarDadosGlobais();
        IdiomaManager idiomaManager = new IdiomaManager();
        idiomaManager.setIdioma(dadosGlobais.getIdiomaPref());

        ConsoleView view = new ConsoleView();
        JogoController jogoCtrl = new JogoController(view, idiomaManager);
        MenuController menuCtrl = new MenuController(view, jogoCtrl, idiomaManager);

        jogoCtrl.setMenuController(menuCtrl);
        menuCtrl.processarMenu();
    }
}
