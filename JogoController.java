import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Controller responsável pelo loop de jogabilidade.
 * Conecta a leitura do JSON, as atualizações do Protagonista e a exibição na View.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class JogoController {
    private ProgressoJogo progresso; // Estado atual do jogo
    private Map<Integer, CenaBase> roteiro; // Repositório de cenas mapeadas por ID
    private ConsoleView view; // Referência da camada View
    private MenuController menuCtrl; // Referência do Controller de Menus
    private Scanner scanner; // Leitor para pausas

    /**
     * Construtor do Controller de Jogo.
     */
    public JogoController(ConsoleView view) {
        this.view = view;
        this.roteiro = new HashMap<>(); // Inicializa o Mapa de roteiro
        this.scanner = new Scanner(System.in); // Leitor auxiliar
    }

    /**
     * Injeta a referência do MenuController para possibilitar retornos.
     */
    public void setMenuController(MenuController mc) {
        this.menuCtrl = mc; // Salva o menu controller
    }

    /**
     * Inicializa os dados da partida e dispara o loop principal.
     */
    public void iniciarNovoJogo(String nome) {
        Protagonista p = new Protagonista(nome); // Instancia o jogador
        this.progresso = new ProgressoJogo(p); // Cria o container de progresso
        
        carregarRoteiroDeArquivo(); // Carrega e decodifica o arquivo roteiro.json
        progresso.setCenaAtual(1); // Posiciona o jogador na Cena 1
        loopDeJogo(); // Inicia a execução do loop narrativo
    }

    /**
     * Lê o arquivo roteiro.json e instancia os objetos CenaJson no repositório.
     */
    public void carregarRoteiroDeArquivo() {
        try {
            // Lê todo o conteúdo textual do arquivo roteiro.json
            String jsonCompleto = new String(Files.readAllBytes(Paths.get("roteiro.json")));
            // Separa os objetos individuais através do divisor de ID
            String[] blocos = jsonCompleto.split("\\{\\s*\"id\"");
            
            for (int i = 1; i < blocos.length; i++) {
                String cenaStr = "{\"id\"" + blocos[i]; // Recompõe a estrutura válida do objeto
                CenaJson cena = new CenaJson(cenaStr); // Instancia a cena executando o parse
                roteiro.put(cena.getId(), cena); // Armazena no mapa de roteiro
            }
            view.exibirMensagem("\n[SISTEMA] Arquivo roteiro.json carregado!");
        } catch (IOException e) {
            view.exibirMensagem("\n[ERRO CRÍTICO] Arquivo roteiro.json não encontrado na pasta.");
            System.exit(1); // Aborta a execução caso o roteiro esteja ausente
        }
    }

    /**
     * Loop principal que gerencia a exibição da cena, captura da escolha e verificação de fim de jogo.
     */
    public void loopDeJogo() {
        while (progresso.getCenaAtual() != -1) {
            CenaBase atual = roteiro.get(progresso.getCenaAtual()); // Resgata a cena do mapa
            
            view.limparTela(); // Limpa o terminal
            
            // Exibe o painel de status atualizado do protagonista
            view.exibirMensagem("========================================================================================");
            view.exibirMensagem("STATUS | Lógica: " + progresso.getJogador().getLogica() + 
                                " | Carisma: " + progresso.getJogador().getCarisma() + 
                                " | Estresse: " + progresso.getJogador().getEstresse() + 
                                " | Coragem: " + progresso.getJogador().getCoragem() + 
                                " | Pistas: " + progresso.getJogador().getPistas() +
                                " | Reputação: " + progresso.getJogador().getReputacaoGlobal());
            view.exibirMensagem("========================================================================================");
            
            view.exibirTextoCena(atual.getTextoNarrativo()); // Exibe o texto da cena
            
            // Tratamento de cena final
            if (atual.isFinal()) {
                view.exibirMensagem("\n=== A HISTÓRIA CHEGOU AO FIM ===");
                view.exibirMensagem("\nPressione [ENTER] para voltar ao Menu Principal...");
                scanner.nextLine(); // Aguarda leitura do usuário
                progresso.setCenaAtual(-1); // Sinaliza encerramento do jogo
                return; // Sai do loop para retornar ao MenuController
            }
            
            // Obtém apenas as opções liberadas pelas condições
            List<Opcao> validas = atual.getOpcoes(progresso.getJogador());
            
            view.exibirMensagem("\nOPÇÕES:");
            for (int i = 0; i < validas.size(); i++) {
                view.exibirMensagem((i + 1) + ". " + validas.get(i).getTexto()); // Lista opções válidas
            }
            view.exibirMensagem("0. [SISTEMA] Pausar e voltar ao Menu Principal");
            
            view.exibirMensagem("\nSua escolha: ");
            
            int escolhaDigitada = -1;
            try {
                // Tenta converter a linha digitada em número inteiro
                escolhaDigitada = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                view.exibirMensagem("Entrada inválida! Por favor, digite um número correspondente a uma opção.");
                view.exibirMensagem("Pressione [ENTER] para continuar...");
                scanner.nextLine();
                continue; // Repete o loop sem travar a execução
            }
            
            if (escolhaDigitada == 0) {
                view.exibirMensagem("\nPartida pausada.");
                return; // Aborta o loop e retorna ao MenuController
            }
            
            int escolhaIdx = escolhaDigitada - 1; // Ajusta índice base 0
            
            if(escolhaIdx >= 0 && escolhaIdx < validas.size()) {
                Opcao escolhida = validas.get(escolhaIdx); // Obtém a opção escolhida
                escolhida.aplicarConsequencias(progresso.getJogador()); // Aplica os impactos nos atributos
                progresso.setCenaAtual(escolhida.getIdProximaCena()); // Atualiza para a nova cena
            } else {
                view.exibirMensagem("Escolha inválida, tente novamente.");
                view.exibirMensagem("Pressione [ENTER] para continuar...");
                scanner.nextLine();
            }
        }
    }
}