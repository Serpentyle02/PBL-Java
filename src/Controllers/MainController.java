/*******************************************************************************
Autor: Renan Queiroz e Felipe Pereira
Componente Curricular: MI de Programação
Concluido em: 18/09/2026
Declaro que este código foi elaborado por mim de forma individual e não contém nenhum
trecho de código de outro colega ou de outro autor, tais como provindos de livros e
apostilas, e páginas ou documentos eletrônicos da Internet. Qualquer trecho de código
de outra autoria que não a minha está destacado com uma citação para o autor e a fonte
do código, e estou ciente que estes trechos não serão considerados para fins de avaliação.
******************************************************************************************/

/**
 * Ponto de entrada (Main) do projeto "O Roubo Quântico".
 * Responsável por instanciar a View, os Controllers e realizar a injeção de dependências.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class MainController {
    
    /**
     * Método principal executado pela JVM.
     * @param args Argumentos da linha de comando.
     */
    public static void main(String[] args) {
        // Instancia a camada de Visão (ConsoleView)
        ConsoleView view = new ConsoleView();
        
        // Instancia o Controller do jogo passando a View
        JogoController jogoCtrl = new JogoController(view);
        
        // Instancia o Controller do Menu conectando a View e o JogoController
        MenuController menuCtrl = new MenuController(view, jogoCtrl);
        
        // Interconecta o MenuController dentro do JogoController para permitir navegação de retorno
        jogoCtrl.setMenuController(menuCtrl); 
        
        // Inicia a execução da aplicação exibindo o menu
        menuCtrl.processarMenu();
    }
}