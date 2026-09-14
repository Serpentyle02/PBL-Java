import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class JogoController {
    private ProgressoJogo progresso;
    private Map<Integer, CenaBase> roteiro; 
    private ConsoleView view;
    private MenuController menuCtrl; 
    private Scanner scanner;

    public JogoController(ConsoleView view) {
        this.view = view;
        this.roteiro = new HashMap<>();
        this.scanner = new Scanner(System.in);
    }

    public void setMenuController(MenuController mc) {
        this.menuCtrl = mc;
    }

    public void iniciarNovoJogo(String nome) {
        Protagonista p = new Protagonista(nome);
        this.progresso = new ProgressoJogo(p);
        
        view.exibirMensagem("\n[SISTEMA]: Você possui 5 pontos para distribuir entre Lógica, Carisma e Estresse.");
        int pontosRestantes = 5;
        
        view.exibirMensagem("Pontos em LÓGICA? (Máx " + pontosRestantes + ")");
        int ptLogica = Integer.parseInt(scanner.nextLine());
        pontosRestantes -= Math.min(ptLogica, pontosRestantes);
        
        view.exibirMensagem("Pontos em CARISMA? (Máx " + pontosRestantes + ")");
        int ptCarisma = Integer.parseInt(scanner.nextLine());
        pontosRestantes -= Math.min(ptCarisma, pontosRestantes);
        
        int ptEstresse = pontosRestantes; 
        view.exibirMensagem("O restante (" + ptEstresse + ") foi alocado em ESTRESSE.");
        
        p.definirAtributosIniciais(ptLogica, ptCarisma, ptEstresse);
        
        carregarRoteiroDeArquivo();
        progresso.setCenaAtual(1);
        loopDeJogo();
    }

    public void carregarRoteiroDeArquivo() {
        try {
            String jsonCompleto = new String(Files.readAllBytes(Paths.get("roteiro.json")));
            String[] blocos = jsonCompleto.split("\\{\\s*\"id\"");
            
            for (int i = 1; i < blocos.length; i++) {
                String cenaStr = "{\"id\"" + blocos[i];
                CenaJson cena = new CenaJson(cenaStr);
                roteiro.put(cena.getId(), cena);
            }
            view.exibirMensagem("\n[SISTEMA] Arquivo roteiro.json carregado com sucesso!");
        } catch (IOException e) {
            view.exibirMensagem("\n[ERRO CRÍTICO] Arquivo roteiro.json não encontrado na pasta.");
            System.exit(1);
        }
    }

    public void loopDeJogo() {
        while (progresso.getCenaAtual() != -1) {
            CenaBase atual = roteiro.get(progresso.getCenaAtual());
            
            view.exibirMensagem("\n=====================================================");
            view.exibirMensagem("STATUS | Lógica: " + progresso.getJogador().getLogica() + 
                                " | Carisma: " + progresso.getJogador().getCarisma() + 
                                " | Estresse: " + progresso.getJogador().getEstresse() + 
                                " | Pistas: " + progresso.getJogador().getPistas() +
                                " | Reputação Geral: " + progresso.getJogador().getReputacaoGlobal());
            view.exibirMensagem("=====================================================");
            
            view.exibirTextoCena(atual.getTextoNarrativo());
            
            if (atual.isFinal()) {
                progresso.setCenaAtual(-1);
                continue;
            }
            
            List<Opcao> validas = atual.getOpcoes(progresso.getJogador());
            
            view.exibirMensagem("\nOPÇÕES:");
            for (int i = 0; i < validas.size(); i++) {
                view.exibirMensagem((i + 1) + ". " + validas.get(i).getTexto());
            }
            view.exibirMensagem("0. [SISTEMA] Pausar e voltar ao Menu Principal");
            
            view.exibirMensagem("\nSua escolha: ");
            
            int escolhaDigitada = -1;
            try {
                escolhaDigitada = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                view.exibirMensagem("Entrada inválida! Por favor, digite um número correspondente a uma opção.");
                continue; 
            }
            
            if (escolhaDigitada == 0) {
                view.exibirMensagem("\nPartida pausada.");
                menuCtrl.processarMenu(); 
                return;
            }
            
            int escolhaIdx = escolhaDigitada - 1;
            
            if(escolhaIdx >= 0 && escolhaIdx < validas.size()) {
                Opcao escolhida = validas.get(escolhaIdx);
                escolhida.aplicarConsequencias(progresso.getJogador()); 
                progresso.setCenaAtual(escolhida.getIdProximaCena());
            } else {
                view.exibirMensagem("Escolha inválida, tente novamente.");
            }
        }
        view.exibirMensagem("\n=== A HISTÓRIA CHEGOU AO FIM ===");
    }
}