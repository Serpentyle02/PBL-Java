public class MainController {
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        JogoController jogoCtrl = new JogoController(view);
        MenuController menuCtrl = new MenuController(view, jogoCtrl);
        
        jogoCtrl.setMenuController(menuCtrl); 
        menuCtrl.processarMenu();
    }
}