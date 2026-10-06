package controllers;

import models.Protagonista;
import models.SalvamentoException;
import views.ConsoleView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JogoController {
    
    private Protagonista protagonista;
    private int idCenaAtual;
    private ConsoleView view;
    
    // Construtor recebendo a View (Injeção de Dependência)
    public JogoController(ConsoleView view) {
        this.view = view;
    }

    /**
     * Inicia a partida.
     * @param carregarSave Se true, tenta ler o arquivo; se false, cria jogo novo.
     */
    public void iniciarPartida(boolean carregarSave) {
        if (carregarSave) {
            carregarProgresso();
        } else {
            iniciarNovoJogo();
        }
        
        loopPrincipal();
    }

    /**
     * Inicializa os dados para um jogo novo do zero.
     */
    private void iniciarNovoJogo() {
        this.protagonista = new Protagonista();
        this.idCenaAtual = 1; // ID da primeira cena do roteiro
    }

    /**
     * Tenta ler o savegame.json na pasta src/JsonFiles. 
     * TRATAMENTO DE ERRO: Se não encontrar o arquivo, gera um novo automaticamente.
     */
    private void carregarProgresso() {
        view.exibirMensagem("Procurando arquivo de salvamento em src/JsonFiles...");

        try {
            // Tenta ler o arquivo do disco na pasta correta
            String json = new String(Files.readAllBytes(Paths.get("src/JsonFiles/savegame.json")));
            
            this.protagonista = new Protagonista();
            
            // Leitura nativa rápida usando Regex para extrair a cena salva
            Matcher mCena = Pattern.compile("\"idCenaAtual\"\\s*:\\s*(\\d+)").matcher(json);
            if (mCena.find()) {
                this.idCenaAtual = Integer.parseInt(mCena.group(1));
            }
            
            view.exibirMensagem("Progresso carregado com sucesso! Retornando à Cena " + this.idCenaAtual);

        } catch (IOException e) {
            // EXCEÇÃO CAPTURADA: Arquivo não existe na pasta
            view.exibirMensagem("AVISO: Arquivo 'savegame.json' não encontrado em src/JsonFiles.");
            view.exibirMensagem("Gerando um novo arquivo de salvamento limpo...");
            
            iniciarNovoJogo(); // Cria um protagonista zerado
            executarSalvamento(); // Força a geração do arquivo novo na pasta correta
        }
    }

    /**
     * Instancia o SalvamentoController e salva o estado atual do jogo.
     */
    public void executarSalvamento() {
        SalvamentoController salvamentoController = new SalvamentoController();
        
        try {
            salvamentoController.salvarProgresso(this.protagonista, this.idCenaAtual);
            view.exibirMensagem(">> Jogo salvo com sucesso em src/JsonFiles/savegame.json! <<");
        } catch (SalvamentoException e) {
            view.exibirMensagem("ERRO CRÍTICO AO SALVAR: " + e.getMessage());
        }
    }

    /**
     * Loop principal de execução do jogo.
     */
    private void loopPrincipal() {
        boolean jogoRodando = true;

        while (jogoRodando) {
            // 1. Limpa a tela antes de desenhar a nova cena
            view.limparTela();
            
            // 2. Exibe o status do jogador (Vida, Estresse, Pistas, etc)
            view.exibirStatusProtagonista(this.protagonista);
            
            // Simulação de menu de opções do turno
            view.exibirMensagem("\nO que você deseja fazer?");
            view.exibirMensagem("[1] Avançar na história");
            view.exibirMensagem("[8] Salvar Jogo");
            view.exibirMensagem("[9] Sair para o Menu Principal");
            
            String entrada = view.lerEntrada();
            int escolha = -1;
            
            // TRATAMENTO DE ERRO: Evita InputMismatchException se o usuário digitar letras
            try {
                escolha = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                view.exibirMensagem("ERRO: Entrada inválida. Por favor, digite apenas números.");
                view.pausarParaLeitura();
                continue; // Reinicia o loop, impedindo o crash
            }
            
            // 3. Processamento da escolha
            switch (escolha) {
                case 1:
                    this.idCenaAtual++; 
                    break;
                case 8:
                    executarSalvamento();
                    view.pausarParaLeitura();
                    break;
                case 9:
                    jogoRodando = false;
                    view.exibirMensagem("Voltando ao menu principal...");
                    break;
                default:
                    view.exibirMensagem("Opção inválida! Tente novamente.");
                    view.pausarParaLeitura();
                    break;
            }
        }
    }
}