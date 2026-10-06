package controllers;

import models.CenaBase;
import models.CenaJson;
import models.Opcao;
import models.Protagonista;
import models.SalvamentoException;
import views.ConsoleView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JogoController {

    private Protagonista protagonista;
    private int idCenaAtual;
    private ConsoleView view;
    private MenuController menuController;

    // Todas as cenas do roteiro, indexadas pelo ID
    private Map<Integer, CenaBase> cenas = new HashMap<>();

    public JogoController(ConsoleView view) {
        this.view = view;
    }

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    /**
     * Chamado pelo MenuController: cria o jogo com o nome informado e inicia o loop.
     */
    public void iniciarNovoJogo(String nome) {
        this.protagonista = new Protagonista(nome);
        this.idCenaAtual = 1;
        loopPrincipal();
    }

    // ------------------------------------------------------------------
    // CARREGAMENTO DO ROTEIRO
    // ------------------------------------------------------------------

    private Path localizarArquivo(String nomeArquivo) {
        String[] bases = {"src/JsonFiles/", "JsonFiles/", "../src/JsonFiles/"};
        for (String base : bases) {
            Path p = Paths.get(base + nomeArquivo);
            if (Files.exists(p)) return p;
        }
        return null;
    }

    private boolean carregarRoteiro() {
        cenas.clear();

        Path caminho = localizarArquivo("Roteiro.json");
        if (caminho == null) {
            view.exibirMensagem("ERRO: 'Roteiro.json' não encontrado.");
            view.exibirMensagem("Coloque-o em src/JsonFiles/. Pasta atual: "
                    + Paths.get("").toAbsolutePath());
            return false;
        }

        try {
            String json = new String(Files.readAllBytes(caminho), StandardCharsets.UTF_8);
            for (String bloco : separarCenas(json)) {
                CenaBase cena = new CenaJson(bloco);
                cenas.put(cena.getId(), cena);
            }
        } catch (IOException | RuntimeException e) {
            view.exibirMensagem("ERRO ao ler o roteiro: " + e.getMessage());
            return false;
        }

        if (cenas.isEmpty()) {
            view.exibirMensagem("ERRO: nenhuma cena foi encontrada no roteiro.");
            return false;
        }
        return true;
    }

    private List<String> separarCenas(String json) {
        List<String> blocos = new ArrayList<>();
        int profundidade = 0;
        int inicio = -1;
        boolean emTexto = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                emTexto = !emTexto;
            }
            if (emTexto) continue;

            if (c == '{') {
                if (profundidade == 0) inicio = i;
                profundidade++;
            } else if (c == '}') {
                profundidade--;
                if (profundidade == 0 && inicio >= 0) {
                    blocos.add(json.substring(inicio, i + 1));
                }
            }
        }
        return blocos;
    }

    // ------------------------------------------------------------------
    // SALVAMENTO
    // ------------------------------------------------------------------

    public void executarSalvamento() {
        SalvamentoController salvamentoController = new SalvamentoController();

        try {
            salvamentoController.salvarProgresso(this.protagonista, this.idCenaAtual);
            view.exibirMensagem(">> Jogo salvo em: "
                    + salvamentoController.caminhoSave().toAbsolutePath() + " <<");
        } catch (SalvamentoException e) {
            view.exibirMensagem("ERRO CRÍTICO AO SALVAR: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // LOOP DA HISTÓRIA
    // ------------------------------------------------------------------

    private void loopPrincipal() {
        if (!carregarRoteiro()) {
            view.pausarParaLeitura();
            return;
        }

        boolean jogoRodando = true;

        while (jogoRodando) {
            CenaBase cena = cenas.get(idCenaAtual);
            if (cena == null) {
                view.exibirMensagem("ERRO: a cena " + idCenaAtual + " não existe no roteiro.");
                view.pausarParaLeitura();
                return;
            }

            view.limparTela();
            view.exibirStatusProtagonista(protagonista);
            view.exibirTextoCena(cena.getTextoNarrativo());

            if (cena.isFinal()) {
                view.exibirMensagem("\n=== FIM DE JOGO ===");
                view.pausarParaLeitura();
                return;
            }

            List<Opcao> opcoes = cena.getOpcoes(protagonista);
            if (opcoes.isEmpty()) {
                view.exibirMensagem("\n(Nenhuma opção disponível nesta cena.)");
                view.pausarParaLeitura();
                return;
            }

            view.exibirMensagem("");
            for (int i = 0; i < opcoes.size(); i++) {
                view.exibirMensagem((i + 1) + ". " + opcoes.get(i).getTexto());
            }
            view.exibirMensagem("\n[S] Salvar jogo   [M] Voltar ao menu principal");

            String entrada = view.lerEntrada().trim().toUpperCase();

            if (entrada.equals("S")) {
                executarSalvamento();
                view.pausarParaLeitura();
                continue;
            }
            if (entrada.equals("M")) {
                view.exibirMensagem("Voltando ao menu principal...");
                jogoRodando = false;
                continue;
            }

            int escolha;
            try {
                escolha = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                view.exibirMensagem("ERRO: digite o número da opção, S ou M.");
                view.pausarParaLeitura();
                continue;
            }

            if (escolha < 1 || escolha > opcoes.size()) {
                view.exibirMensagem("Opção inválida! Tente novamente.");
                view.pausarParaLeitura();
                continue;
            }

            Opcao escolhida = opcoes.get(escolha - 1);
            escolhida.aplicarConsequencias(protagonista);
            idCenaAtual = escolhida.getIdProximaCena();
        }
    }
}