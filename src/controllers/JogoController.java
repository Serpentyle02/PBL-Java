package controllers;

import models.CenaJson;
import models.Opcao;
import models.ProgressoJogo;
import models.Protagonista;
import models.SalvamentoException;
import views.ConsoleView;

import java.util.List;

public class JogoController {

    private Protagonista protagonista;
    private int idCenaAtual;
    private int slotAtual = 1;

    private final ConsoleView view;
    private final SalvamentoController saveCtrl;
    private List<CenaJson> roteiroCenas;
    private ProgressoJogo dadosGlobais;
    private MenuController menuCtrl;

    public JogoController(ConsoleView view) {
        this.view = view;
        this.saveCtrl = new SalvamentoController();
        this.dadosGlobais = saveCtrl.carregarDadosGlobais();
        this.roteiroCenas = CenaJson.carregarCenasDoArquivo();
    }

    public void setMenuController(MenuController menuCtrl) {
        this.menuCtrl = menuCtrl;
    }

    public void iniciarNovoJogo(String nome) {
        this.protagonista = new Protagonista(nome);
        this.slotAtual = 0;
        this.dadosGlobais = saveCtrl.carregarDadosGlobais();
        recarregarRoteiroSeNecessario();
        this.idCenaAtual = obterIdPrimeiraCena();
        loopPrincipal();
    }

    public void iniciarPartida(boolean carregarSave) {
        if (carregarSave && !gerenciarMenuCarregamento()) return;
        this.dadosGlobais = saveCtrl.carregarDadosGlobais();
        recarregarRoteiroSeNecessario();
        loopPrincipal();
    }

    private void recarregarRoteiroSeNecessario() {
        if (roteiroCenas == null || roteiroCenas.isEmpty()) {
            roteiroCenas = CenaJson.carregarCenasDoArquivo();
        }
    }

    private int obterIdPrimeiraCena() {
        return (roteiroCenas != null && !roteiroCenas.isEmpty()) ? roteiroCenas.get(0).getId() : 1;
    }

    private void loopPrincipal() {
        if (roteiroCenas == null || roteiroCenas.isEmpty()) {
            view.exibirMensagem("[ERRO] Roteiro vazio ou não encontrado. Não é possível iniciar.");
            view.pausarParaLeitura();
            return;
        }

        boolean rodando = true;
        while (rodando) {
            CenaJson cena = buscarCenaPorId(idCenaAtual);
            if (cena == null) {
                view.exibirMensagem("[ERRO] Cena " + idCenaAtual + " não existe no roteiro.");
                view.pausarParaLeitura();
                return;
            }

            view.limparTela();
            view.exibirStatusProtagonista(protagonista);
            if (dadosGlobais.isExibirDicas()) {
                view.exibirMensagem("[Dica: Suas decisões alteram atributos e liberam novos finais]");
            }

            String resumoCena = renderizarCena(cena);

            if (cena.isFinal() || cena.getOpcoes().isEmpty()) {
                registrarFinal(cena);
                view.pausarParaLeitura();
                return;
            }

            exibirMenuAcoesDaCena(cena);
            int escolha = lerOpcaoNumerica();

            if (escolha == 7) {
                gerenciarMenuPreferencias();
            } else if (escolha == 8) {
                gerenciarMenuSalvamento(resumoCena);
            } else if (escolha == 9) {
                rodando = false;
            } else if (escolha >= 1 && escolha <= cena.getOpcoes().size()) {
                Opcao op = cena.getOpcoes().get(escolha - 1);

                if (!op.verificarCondicao(protagonista)) {
                    view.exibirMensagem("\n[BLOQUEADO] Você não possui os requisitos para esta escolha!");
                    view.pausarParaLeitura();
                    continue;
                }

                String mudancas = op.aplicarConsequencias(protagonista);
                if (!mudancas.isEmpty()) {
                    view.exibirMensagem("\n>> Impacto: " + mudancas);
                    view.pausarParaLeitura();
                }

                if (buscarCenaPorId(op.getIdProximaCena()) != null) {
                    idCenaAtual = op.getIdProximaCena();
                } else {
                    view.exibirMensagem("[ERRO] Destino inválido (" + op.getIdProximaCena() + ") no roteiro.");
                    view.pausarParaLeitura();
                }
            } else {
                view.exibirMensagem("Opção inválida!");
                view.pausarParaLeitura();
            }
        }
    }

    private String renderizarCena(CenaJson cena) {
        if (!cena.getTitulo().isEmpty()) {
            view.exibirMensagem("\n=== " + cena.getTitulo() + " ===\n");
        }
        view.exibirNarrativaComPreferencia(cena.getTexto(), dadosGlobais.getVelocidadeTextoMs());
        processarEventosDaCena();

        String txt = cena.getTexto().replace("\n", " ");
        return txt.length() > 25 ? txt.substring(0, 25) + "..." : txt;
    }

    private void exibirMenuAcoesDaCena(CenaJson cena) {
        view.exibirMensagem("\nEscolhas disponíveis:");
        List<Opcao> ops = cena.getOpcoes();
        for (int i = 0; i < ops.size(); i++) {
            String marca = ops.get(i).verificarCondicao(protagonista) ? "" : " (bloqueada)";
            view.exibirMensagem("[" + (i + 1) + "] " + ops.get(i).getTexto() + marca);
        }
        view.exibirMensagem("------------------------------------------");
        view.exibirMensagem("[7] Preferências | [8] Salvar (3 Slots) | [9] Menu Principal");
    }

    private void processarEventosDaCena() {
        try {
            if (idCenaAtual == 2 && dadosGlobais.adicionarDesbloqueavel("Conquista: Primeiros Passos na Investigação")) {
                view.exibirMensagem("\n[!] DESBLOQUEADO NA GALERIA: 'Conquista: Primeiros Passos na Investigação'");
                saveCtrl.salvarDadosGlobais(dadosGlobais);
            }
            if (idCenaAtual == 5) {
                if (dadosGlobais.adicionarDesbloqueavel("Dossiê Confidencial: A Testemunha Oculta")) {
                    view.exibirMensagem("\n[!] DESBLOQUEADO NA GALERIA: 'Dossiê Confidencial: A Testemunha Oculta'");
                    saveCtrl.salvarDadosGlobais(dadosGlobais);
                }
                if (dadosGlobais.isAutoSaveAtivo()) {
                    if (slotAtual == 0) slotAtual = primeiroSlotVazio();
                    if (slotAtual == 0) {
                        view.exibirMensagem("[Auto-Save ignorado: todos os slots estão ocupados. Salve manualmente.]");
                    } else {
                        saveCtrl.salvarSlot(slotAtual, protagonista, idCenaAtual, "Auto-Save");
                        view.exibirMensagem("[Auto-Save concluído no Slot " + slotAtual + "]");
                    }
                }
            }
        } catch (SalvamentoException e) {
            view.exibirMensagem("Aviso de persistência: " + e.getMessage());
        }
    }

    private int primeiroSlotVazio() {
        for (int i = 1; i <= 3; i++) {
            if (!saveCtrl.existeSlot(i)) return i;
        }
        return 0;
    }

    private void registrarFinal(CenaJson cena) {
        String nome = "Final: " + (cena.getTitulo().isEmpty() ? "Cena " + cena.getId() : cena.getTitulo());
        try {
            if (dadosGlobais.adicionarFinal(nome)) {
                view.exibirMensagem("\n*** NOVO FINAL DESCOBERTO E REGISTRADO: " + nome + " ***");
                saveCtrl.salvarDadosGlobais(dadosGlobais);
            } else {
                view.exibirMensagem("\n*** FIM DE JOGO: " + nome + " (Já registrado) ***");
            }
        } catch (SalvamentoException e) {
            view.exibirMensagem("Aviso ao salvar final: " + e.getMessage());
        }
    }

    public void gerenciarMenuSalvamento(String resumoCena) {
        exibirListaDeSlots("SALVAR PARTIDA");
        view.exibirMensagem("Escolha o Slot (1 a 3) ou [0] para cancelar:");

        int slot = lerOpcaoNumerica();
        if (slot < 1 || slot > 3) return;

        if (saveCtrl.existeSlot(slot) && !confirmarAcao("O Slot " + slot + " já possui um save. Deseja sobrescrever?")) {
            view.exibirMensagem("Salvamento cancelado.");
            view.pausarParaLeitura();
            return;
        }

        try {
            saveCtrl.salvarSlot(slot, protagonista, idCenaAtual, resumoCena);
            this.slotAtual = slot;
            view.exibirMensagem(">> Jogo salvo com sucesso no Slot " + slot + "! <<");
        } catch (SalvamentoException e) {
            view.exibirMensagem("ERRO: " + e.getMessage());
        }
        view.pausarParaLeitura();
    }

    public boolean gerenciarMenuCarregamento() {
        while (true) {
            exibirListaDeSlots("CARREGAR OU EXCLUIR PARTIDA");
            view.exibirMensagem("[1-3] Carregar Slot | [4] Excluir Slot | [0] Voltar");

            int op = lerOpcaoNumerica();
            if (op == 0) return false;

            if (op >= 1 && op <= 3) {
                try {
                    ProgressoJogo save = saveCtrl.carregarSlot(op);
                    this.protagonista = save.getProtagonista();
                    this.idCenaAtual = save.getIdCenaAtual();
                    this.slotAtual = op;
                    view.exibirMensagem(">> Slot " + op + " carregado com sucesso! <<");
                    view.pausarParaLeitura();
                    return true;
                } catch (SalvamentoException e) {
                    view.exibirMensagem("AVISO: " + e.getMessage());
                    view.pausarParaLeitura();
                }
            } else if (op == 4) {
                executarExclusaoDeSlot();
            }
        }
    }

    private void executarExclusaoDeSlot() {
        view.exibirMensagem("Digite o Slot que deseja EXCLUIR (1 a 3):");
        int slot = lerOpcaoNumerica();

        if (slot >= 1 && slot <= 3 && saveCtrl.existeSlot(slot)) {
            if (confirmarAcao("Deseja excluir permanentemente o Slot " + slot + "?")) {
                try {
                    saveCtrl.excluirSlot(slot);
                    view.exibirMensagem(">> Slot " + slot + " excluído! <<");
                } catch (SalvamentoException e) {
                    view.exibirMensagem("ERRO: " + e.getMessage());
                }
            }
        } else {
            view.exibirMensagem("Slot inválido ou já está vazio.");
        }
        view.pausarParaLeitura();
    }

    public void gerenciarMenuPreferencias() {
        boolean configurando = true;
        while (configurando) {
            view.limparTela();
            view.exibirMensagem("=== PREFERÊNCIAS DO USUÁRIO ===");
            view.exibirMensagem("[1] Velocidade do Texto: " + (dadosGlobais.getVelocidadeTextoMs() == 0 ? "Instantâneo" : "Lento (20ms)"));
            view.exibirMensagem("[2] Salvamento Automático: " + (dadosGlobais.isAutoSaveAtivo() ? "ATIVADO" : "DESATIVADO"));
            view.exibirMensagem("[3] Exibir Dicas de Jogo: " + (dadosGlobais.isExibirDicas() ? "ATIVADO" : "DESATIVADO"));
            view.exibirMensagem("[0] Salvar e Voltar");

            switch (lerOpcaoNumerica()) {
                case 1:
                    dadosGlobais.setVelocidadeTextoMs(dadosGlobais.getVelocidadeTextoMs() == 0 ? 20 : 0);
                    break;
                case 2:
                    dadosGlobais.setAutoSaveAtivo(!dadosGlobais.isAutoSaveAtivo());
                    break;
                case 3:
                    dadosGlobais.setExibirDicas(!dadosGlobais.isExibirDicas());
                    break;
                case 0:
                    configurando = false;
                    break;
                default:
                    break;
            }
        }

        try {
            saveCtrl.salvarDadosGlobais(dadosGlobais);
            view.exibirMensagem(">> Preferências atualizadas! <<");
        } catch (SalvamentoException e) {
            view.exibirMensagem("Erro ao salvar preferências: " + e.getMessage());
        }
        view.pausarParaLeitura();
    }

    public void exibirGaleriaEFinais() {
        dadosGlobais = saveCtrl.carregarDadosGlobais();

        view.limparTela();
        view.exibirMensagem("=== CONTEÚDOS DESBLOQUEADOS (GALERIA) ===");
        if (dadosGlobais.getDesbloqueaveis().isEmpty()) {
            view.exibirMensagem("Nenhum conteúdo desbloqueado ainda.");
        } else {
            for (String item : dadosGlobais.getDesbloqueaveis()) view.exibirMensagem(" [X] " + item);
        }

        view.exibirMensagem("\n=== REGISTRO DE FINAIS ALCANÇADOS ===");
        if (dadosGlobais.getFinais().isEmpty()) {
            view.exibirMensagem("Nenhum final alcançado ainda.");
        } else {
            for (String fim : dadosGlobais.getFinais()) view.exibirMensagem(" * " + fim);
        }
        view.pausarParaLeitura();
    }

    private void exibirListaDeSlots(String titulo) {
        view.limparTela();
        view.exibirMensagem("=== " + titulo + " ===");
        for (int i = 1; i <= 3; i++) view.exibirMensagem(saveCtrl.obterResumoVisualSlot(i));
    }

    private boolean confirmarAcao(String pergunta) {
        view.exibirMensagem(pergunta + " (S/N):");
        return view.lerEntrada().trim().equalsIgnoreCase("S");
    }

    private CenaJson buscarCenaPorId(int id) {
        if (roteiroCenas == null) return null;
        for (CenaJson c : roteiroCenas) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    private int lerOpcaoNumerica() {
        try {
            return Integer.parseInt(view.lerEntrada().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}