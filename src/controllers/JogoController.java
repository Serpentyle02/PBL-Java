package controllers;

import models.CenaBase;
import models.CenaJson;
import models.Conquista;
import models.Conquistas;
import models.IdiomaManager;
import models.Opcao;
import models.ProgressoJogo;
import models.Protagonista;
import models.SalvamentoException;
import views.ConsoleView;

import java.util.List;
import java.util.Map;

/**
 * Controlador principal da lógica narrativa e do estado de execução da partida (Controller no MVC).
 * Coordena a progressão entre as cenas do roteiro, validação de requisitos, aplicação de
 * consequências nos atributos do protagonista, eventos especiais (conquistas e auto-save),
 * suporte multi-idioma e os submenus de salvamento, carregamento, preferências e galeria.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class JogoController {

    // Estado da sessão atual do jogador
    private Protagonista protagonista;
    private int idCenaAtual;
    // Slot associado à partida atual (0 indica partida nova ainda sem slot vinculado)
    private int slotAtual = 1;

    // Associações/agregações com View, gerenciador de idioma, persistência, mapa polimórfico de cenas e dados globais
    private final ConsoleView view;
    private final IdiomaManager idiomaManager;
    private final SalvamentoController saveCtrl;
    private Map<Integer, CenaBase> roteiroCenas;
    private String idiomaRoteiroCarregado;
    private ProgressoJogo dadosGlobais;
    @SuppressWarnings("unused")
    private MenuController menuCtrl;

    /**
     * Construtor do JogoController com gerenciador de idioma compartilhado.
     * Carrega os dados globais e todas as cenas do roteiro no idioma ativo.
     *
     * @param view          Instância da {@link ConsoleView} compartilhada pela aplicação.
     * @param idiomaManager Gerenciador de textos e idioma da interface.
     */
    public JogoController(ConsoleView view, IdiomaManager idiomaManager) {
        this.view = view;
        this.idiomaManager = (idiomaManager != null) ? idiomaManager : new IdiomaManager();
        this.saveCtrl = new SalvamentoController();
        this.dadosGlobais = saveCtrl.carregarDadosGlobais();
        this.idiomaManager.setIdioma(dadosGlobais.getIdiomaPref());
        this.idiomaRoteiroCarregado = this.idiomaManager.getIdiomaAtual();
        this.roteiroCenas = CenaJson.carregarMapaCenasDoArquivo(this.idiomaRoteiroCarregado);
    }

    /**
     * Construtor alternativo que instancia um {@link IdiomaManager} padrão.
     *
     * @param view Instância da {@link ConsoleView} compartilhada pela aplicação.
     */
    public JogoController(ConsoleView view) {
        this(view, new IdiomaManager());
    }

    /**
     * Registra a referência para o {@link MenuController}.
     *
     * @param menuCtrl Controlador do menu principal.
     */
    public void setMenuController(MenuController menuCtrl) {
        this.menuCtrl = menuCtrl;
    }

    /**
     * Inicia uma nova partida do zero com o nome informado para o protagonista.
     * Desvincula o slot atual ({@code slotAtual = 0}) para que um eventual auto-save
     * não sobrescreva saves existentes de outras partidas.
     *
     * @param nome Nome do protagonista.
     */
    public void iniciarNovoJogo(String nome) {
        this.protagonista = new Protagonista(nome);
        this.slotAtual = 0;
        this.dadosGlobais = saveCtrl.carregarDadosGlobais();
        recarregarRoteiroSeNecessario();
        this.idCenaAtual = obterIdPrimeiraCena();
        loopPrincipal();
    }

    /**
     * Inicia uma partida a partir do menu de carregamento de saves (quando {@code carregarSave == true}).
     * Caso o usuário cancele e volte ao menu principal, encerra sem entrar no loop narrativo.
     *
     * @param carregarSave Indica se deve abrir a seleção de slots salvos antes de iniciar.
     */
    public void iniciarPartida(boolean carregarSave) {
        if (carregarSave && !gerenciarMenuCarregamento()) return;
        this.dadosGlobais = saveCtrl.carregarDadosGlobais();
        recarregarRoteiroSeNecessario();
        loopPrincipal();
    }

    /**
     * Retoma imediatamente a partida salva mais recentemente (opção "Continuar" do menu principal),
     * sem passar pela seleção manual de slots. Se não houver nenhum save, avisa o jogador.
     */
    public void continuarUltimaPartida() {
        int slot = saveCtrl.obterSlotMaisRecente();
        if (slot == 0) {
            view.limparTela();
            view.exibirMensagem(idiomaManager.get("SEM_SAVE_CONTINUAR"));
            view.pausarParaLeitura(idiomaManager);
            return;
        }
        try {
            ProgressoJogo save = saveCtrl.carregarSlot(slot);
            this.protagonista = save.getProtagonista();
            this.idCenaAtual = save.getIdCenaAtual();
            this.slotAtual = slot;
        } catch (SalvamentoException e) {
            view.exibirMensagem("AVISO: " + e.getMessage());
            view.pausarParaLeitura(idiomaManager);
            return;
        }
        this.dadosGlobais = saveCtrl.carregarDadosGlobais();
        recarregarRoteiroSeNecessario();
        loopPrincipal();
    }

    /**
     * Devolve o título (capítulo) da cena atual, usado nos metadados dos saves.
     */
    private String tituloDaCenaAtual() {
        CenaBase cena = buscarCenaPorId(idCenaAtual);
        return (cena != null && !cena.getTitulo().isEmpty()) ? cena.getTitulo() : "-";
    }

    /**
     * Garante que o idioma ativo e o mapa de cenas do roteiro estejam sincronizados em memória.
     */
    private void recarregarRoteiroSeNecessario() {
        idiomaManager.setIdioma(dadosGlobais.getIdiomaPref());
        String idiomaAtual = idiomaManager.getIdiomaAtual();
        if (roteiroCenas == null || roteiroCenas.isEmpty() || !idiomaAtual.equals(idiomaRoteiroCarregado)) {
            idiomaRoteiroCarregado = idiomaAtual;
            roteiroCenas = CenaJson.carregarMapaCenasDoArquivo(idiomaAtual);
        }
    }

    /**
     * Obtém o ID da primeira cena disponível no roteiro (por padrão, 1).
     */
    private int obterIdPrimeiraCena() {
        return (roteiroCenas != null && !roteiroCenas.isEmpty()) ? roteiroCenas.keySet().iterator().next() : 1;
    }

    /**
     * Loop central da partida. A cada ciclo:
     * <ol>
     *   <li>Localiza a cena atual pelo ID;</li>
     *   <li>Limpa o terminal e exibe a barra de status e dicas;</li>
     *   <li>Renderiza a narrativa e verifica se atingiu um final;</li>
     *   <li>Exibe as opções de decisão (marcando as bloqueadas) e os atalhos [7], [8] e [9];</li>
     *   <li>Processa a escolha do jogador, aplicando consequências e avançando para a próxima cena.</li>
     * </ol>
     */
    private void loopPrincipal() {
        if (roteiroCenas == null || roteiroCenas.isEmpty()) {
            view.exibirMensagem(idiomaManager.get("ERRO_ROTEIRO_VAZIO"));
            view.pausarParaLeitura(idiomaManager);
            return;
        }

        boolean rodando = true;
        while (rodando) {
            CenaBase cena = buscarCenaPorId(idCenaAtual);
            if (cena == null) {
                view.exibirMensagem(idiomaManager.get("ERRO_CENA_INEXISTENTE") + idCenaAtual);
                view.pausarParaLeitura(idiomaManager);
                return;
            }

            view.limparTela();
            view.exibirStatusProtagonista(protagonista, idiomaManager);
            if (dadosGlobais.isExibirDicas()) {
                view.exibirMensagem(idiomaManager.get("DICA_ATRIBUTOS"));
            }

            String resumoCena = renderizarCena(cena);

            // Se a cena for um desfecho, registra o final alcançado e retorna ao menu principal
            if (cena.isFinal() || cena.getOpcoes().isEmpty()) {
                registrarFinal(cena);
                verificarConquistas(cena); // reavalia agora que o final já foi registrado
                view.pausarParaLeitura(idiomaManager);
                return;
            }

            exibirMenuAcoesDaCena(cena);
            int escolha = lerOpcaoNumerica();

            if (escolha == 7) {
                gerenciarMenuPreferencias();
            } else if (escolha == 8) {
                gerenciarMenuSalvamento(resumoCena);
            } else if (escolha == 9) {
                rodando = false; // Retorna ao Menu Principal
            } else if (escolha >= 1 && escolha <= cena.getOpcoes().size()) {
                Opcao op = cena.getOpcoes().get(escolha - 1);

                // Impede o avanço caso o protagonista não cumpra os requisitos da opção
                if (!op.verificarCondicao(protagonista)) {
                    view.exibirMensagem(idiomaManager.get("OPCAO_BLOQUEADA"));
                    view.pausarParaLeitura(idiomaManager);
                    continue;
                }

                // Aplica os deltas de atributos/relacionamentos e mostra o feedback ao jogador
                String mudancas = op.aplicarConsequencias(protagonista);
                if (!mudancas.isEmpty()) {
                    view.exibirMensagem(idiomaManager.get("IMPACTO") + mudancas);
                    view.pausarParaLeitura(idiomaManager);
                }

                // Valida se a cena de destino existe antes de atualizar o ponteiro da cena atual
                if (buscarCenaPorId(op.getIdProximaCena()) != null) {
                    idCenaAtual = op.getIdProximaCena();
                } else {
                    view.exibirMensagem(idiomaManager.get("ERRO_DESTINO_INVALIDO") + op.getIdProximaCena());
                    view.pausarParaLeitura(idiomaManager);
                }
            } else {
                view.exibirMensagem(idiomaManager.get("OPCAO_INVALIDA"));
                view.pausarParaLeitura(idiomaManager);
            }
        }
    }

    /**
     * Exibe o título temático e a narrativa da cena atual, dispara eventos especiais
     * e devolve um resumo curto (primeiros 25 caracteres) para uso em salvamentos.
     */
    private String renderizarCena(CenaBase cena) {
        if (!cena.getTitulo().isEmpty()) {
            view.exibirMensagem("\n=== " + cena.getTitulo() + " ===\n");
        }
        view.exibirNarrativaComPreferencia(cena.getTexto(), dadosGlobais.getVelocidadeTextoMs());
        verificarConquistas(cena);
        processarEventosDaCena();

        String txt = cena.getTexto().replace("\n", " ");
        return txt.length() > 25 ? txt.substring(0, 25) + "..." : txt;
    }

    /**
     * Lista numeradamente todas as opções da cena (indicando quais estão bloqueadas)
     * e a barra inferior de comandos auxiliares (Preferências, Salvar e Menu Principal).
     */
    private void exibirMenuAcoesDaCena(CenaBase cena) {
        view.exibirMensagem(idiomaManager.get("ESCOLHAS_DISPONIVEIS"));
        List<Opcao> ops = cena.getOpcoes();
        for (int i = 0; i < ops.size(); i++) {
            String marca = ops.get(i).verificarCondicao(protagonista) ? "" : idiomaManager.get("MARCA_BLOQUEADA");
            view.exibirMensagem("[" + (i + 1) + "] " + ops.get(i).getTexto() + marca);
        }
        view.exibirMensagem(idiomaManager.get("MENU_RODAPE_JOGO"));
    }

    /**
     * Avalia o catálogo de {@link Conquistas} no estado atual da partida, avisa o jogador
     * sobre cada conquista recém-desbloqueada no idioma ativo e persiste os dados globais.
     *
     * @param cena Cena em exibição (define se o estado atual é um final).
     */
    private void verificarConquistas(CenaBase cena) {
        boolean cenaFinal = cena.isFinal() || cena.getOpcoes().isEmpty();
        List<Conquista> novas = Conquistas.verificar(protagonista, cena.getId(), cenaFinal, dadosGlobais);
        if (novas.isEmpty()) return;
        String idioma = idiomaManager.getIdiomaAtual();
        for (Conquista c : novas) {
            view.exibirMensagem(idiomaManager.get("AVISO_DESBLOQUEIO") + c.getTitulo(idioma) + " - " + c.getDescricao(idioma));
        }
        try {
            saveCtrl.salvarDadosGlobais(dadosGlobais);
        } catch (SalvamentoException e) {
            view.exibirMensagem(idiomaManager.get("ERRO_PERSISTENCIA") + e.getMessage());
        }
    }

    /**
     * Executa o salvamento automático (Auto-Save) ao chegar na Cena 5, em um slot seguro.
     */
    private void processarEventosDaCena() {
        try {
            if (idCenaAtual == 5 && dadosGlobais.isAutoSaveAtivo()) {
                // Se a partida ainda não tem slot próprio, busca o primeiro slot livre
                if (slotAtual == 0) slotAtual = primeiroSlotVazio();
                if (slotAtual == 0) {
                    view.exibirMensagem(idiomaManager.get("AUTOSAVE_IGNORADO"));
                } else {
                    saveCtrl.salvarSlot(slotAtual, protagonista, idCenaAtual, tituloDaCenaAtual(), "Auto-Save");
                    view.exibirMensagem(idiomaManager.get("AUTOSAVE_CONCLUIDO") + slotAtual + "]");
                }
            }
        } catch (SalvamentoException e) {
            view.exibirMensagem(idiomaManager.get("ERRO_PERSISTENCIA") + e.getMessage());
        }
    }

    /**
     * Retorna o número do primeiro slot vazio (1 a 3), ou 0 caso todos os três estejam ocupados.
     */
    private int primeiroSlotVazio() {
        for (int i = 1; i <= 3; i++) {
            if (!saveCtrl.existeSlot(i)) return i;
        }
        return 0;
    }

    /**
     * Registra o final alcançado na galeria global e informa ao jogador se é uma descoberta inédita.
     * Evita duplicidade caso o mesmo final (1 a 4) já tenha sido registrado em outro idioma.
     */
    private void registrarFinal(CenaBase cena) {
        String titulo = cena.getTitulo().isEmpty() ? "-" : cena.getTitulo();
        String nome = idiomaManager.get("PREFIXO_FINAL") + titulo;
        try {
            if (!finalJaDescobertoEmQualquerIdioma(cena.getId(), nome) && dadosGlobais.adicionarFinal(nome)) {
                view.exibirMensagem(idiomaManager.get("FINAL_DESCOBERTO") + nome + " ***");
                saveCtrl.salvarDadosGlobais(dadosGlobais);
            } else {
                view.exibirMensagem(idiomaManager.get("FINAL_JA_REGISTRADO") + nome + " ***");
            }
        } catch (SalvamentoException e) {
            view.exibirMensagem(idiomaManager.get("ERRO_PERSISTENCIA") + e.getMessage());
        }
    }

    /**
     * Verifica se o desfecho correspondente ao ID da cena final já consta na lista de finais,
     * independentemente do idioma em que tenha sido registrado anteriormente.
     */
    private boolean finalJaDescobertoEmQualquerIdioma(int idCena, String nomeAtual) {
        if (dadosGlobais.getFinais().contains(nomeAtual)) return true;
        String marcadorNumero;
        switch (idCena) {
            case 201: marcadorNumero = " 1 -"; break;
            case 21:  marcadorNumero = " 2 -"; break;
            case 22:  marcadorNumero = " 3 -"; break;
            case 23:  marcadorNumero = " 4 -"; break;
            default:  return false;
        }
        for (String registrado : dadosGlobais.getFinais()) {
            if (registrado.contains("FINAL" + marcadorNumero) || registrado.contains("ENDING" + marcadorNumero)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Exibe o submenu de salvamento em um dos 3 slots, solicitando confirmação antes de sobrescrever.
     *
     * @param resumoCena Breve texto identificador da cena atual.
     */
    public void gerenciarMenuSalvamento(String resumoCena) {
        exibirListaDeSlots(idiomaManager.get("TITULO_SALVAR_PARTIDA"));
        view.exibirMensagem(idiomaManager.get("ESCOLHA_SLOT_SALVAR"));

        int slot = lerOpcaoNumerica();
        if (slot < 1 || slot > 3) return;

        if (saveCtrl.existeSlot(slot) && !confirmarAcao(idiomaManager.get("CONFIRMAR_SOBRESCREVER") + slot + "?")) {
            view.exibirMensagem(idiomaManager.get("SALVAMENTO_CANCELADO"));
            view.pausarParaLeitura(idiomaManager);
            return;
        }

        try {
            saveCtrl.salvarSlot(slot, protagonista, idCenaAtual, tituloDaCenaAtual(), resumoCena);
            this.slotAtual = slot;
            view.exibirMensagem(idiomaManager.get("SUCESSO_SALVAR_SLOT") + slot + "! <<");
        } catch (SalvamentoException e) {
            view.exibirMensagem("ERRO: " + e.getMessage());
        }
        view.pausarParaLeitura(idiomaManager);
    }

    /**
     * Exibe o submenu para carregar ou excluir qualquer um dos 3 slots de salvamento.
     *
     * @return {@code true} se um slot foi carregado com sucesso; {@code false} se o usuário voltou ao menu.
     */
    public boolean gerenciarMenuCarregamento() {
        while (true) {
            exibirListaDeSlots(idiomaManager.get("TITULO_CARREGAR_PARTIDA"));
            view.exibirMensagem(idiomaManager.get("OPCOES_MENU_CARREGAR"));

            int op = lerOpcaoNumerica();
            if (op == 0) return false;

            if (op >= 1 && op <= 3) {
                try {
                    ProgressoJogo save = saveCtrl.carregarSlot(op);
                    this.protagonista = save.getProtagonista();
                    this.idCenaAtual = save.getIdCenaAtual();
                    this.slotAtual = op;
                    view.exibirMensagem(idiomaManager.get("SUCESSO_CARREGAR_SLOT") + op + "! <<");
                    view.pausarParaLeitura(idiomaManager);
                    return true;
                } catch (SalvamentoException e) {
                    view.exibirMensagem("AVISO: " + e.getMessage());
                    view.pausarParaLeitura(idiomaManager);
                }
            } else if (op == 4) {
                executarExclusaoDeSlot();
            }
        }
    }

    /**
     * Solicita o número do slot e confirmação do usuário para excluir permanentemente o save.
     */
    private void executarExclusaoDeSlot() {
        view.exibirMensagem(idiomaManager.get("ESCOLHA_SLOT_EXCLUIR"));
        int slot = lerOpcaoNumerica();

        if (slot >= 1 && slot <= 3 && saveCtrl.existeSlot(slot)) {
            if (confirmarAcao(idiomaManager.get("CONFIRMAR_EXCLUIR") + slot + "?")) {
                try {
                    saveCtrl.excluirSlot(slot);
                    view.exibirMensagem(idiomaManager.get("SUCESSO_EXCLUIR_SLOT") + slot + "! <<");
                } catch (SalvamentoException e) {
                    view.exibirMensagem("ERRO: " + e.getMessage());
                }
            }
        } else {
            view.exibirMensagem(idiomaManager.get("ERRO_SLOT_INVALIDO"));
        }
        view.pausarParaLeitura(idiomaManager);
    }

    /**
     * Exibe e processa o menu de preferências do usuário (velocidade do texto, auto-save, dicas e idioma),
     * persistindo as alterações em {@code dados_globais.json} ao sair.
     */
    public void gerenciarMenuPreferencias() {
        boolean configurando = true;
        while (configurando) {
            view.limparTela();
            view.exibirMensagem(idiomaManager.get("PREF_TITULO"));
            view.exibirMensagem("[1] " + idiomaManager.get("PREF_VELOCIDADE")
                    + (dadosGlobais.getVelocidadeTextoMs() == 0 ? idiomaManager.get("INSTANTANEO") : idiomaManager.get("LENTO")));
            view.exibirMensagem("[2] " + idiomaManager.get("PREF_AUTOSAVE")
                    + (dadosGlobais.isAutoSaveAtivo() ? idiomaManager.get("ATIVADO") : idiomaManager.get("DESATIVADO")));
            view.exibirMensagem("[3] " + idiomaManager.get("PREF_DICAS")
                    + (dadosGlobais.isExibirDicas() ? idiomaManager.get("ATIVADO") : idiomaManager.get("DESATIVADO")));
            view.exibirMensagem("[4] " + idiomaManager.get("PREF_IDIOMA") + ": " + dadosGlobais.getIdiomaPref());
            view.exibirMensagem("[0] " + idiomaManager.get("SALVAR_VOLTAR"));

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
                case 4:
                    String novoIdioma = view.exibirMenuIdiomas(dadosGlobais.getIdiomaPref());
                    dadosGlobais.setIdiomaPref(novoIdioma);
                    idiomaManager.setIdioma(novoIdioma);
                    idiomaRoteiroCarregado = idiomaManager.getIdiomaAtual();
                    roteiroCenas = CenaJson.carregarMapaCenasDoArquivo(idiomaRoteiroCarregado);
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
            view.exibirMensagem(">> " + idiomaManager.get("PREF_ATUALIZADAS") + " <<");
        } catch (SalvamentoException e) {
            view.exibirMensagem(idiomaManager.get("ERRO_PERSISTENCIA") + e.getMessage());
        }
        view.pausarParaLeitura(idiomaManager);
    }

    /**
     * Exibe a Galeria contendo todas as conquistas/dossiês desbloqueados (com contador de progresso)
     * e os finais já descobertos pelo jogador, traduzidos para o idioma atual.
     */
    public void exibirGaleriaEFinais() {
        dadosGlobais = saveCtrl.carregarDadosGlobais();
        recarregarRoteiroSeNecessario();
        String idioma = idiomaManager.getIdiomaAtual();

        view.limparTela();
        List<Conquista> todas = Conquistas.todas();
        int obtidas = 0;
        for (Conquista c : todas) {
            if (dadosGlobais.getDesbloqueaveis().contains(c.getTitulo())) obtidas++;
        }
        view.exibirMensagem(idiomaManager.get("TITULO_GALERIA") + " (" + obtidas + "/" + todas.size() + ") ===");
        for (Conquista c : todas) {
            if (dadosGlobais.getDesbloqueaveis().contains(c.getTitulo())) {
                view.exibirMensagem(" [X] " + c.getTitulo(idioma) + " - " + c.getDescricao(idioma));
            } else {
                view.exibirMensagem(" [ ] ???");
            }
        }

        view.exibirMensagem(idiomaManager.get("TITULO_FINAIS"));
        if (dadosGlobais.getFinais().isEmpty()) {
            view.exibirMensagem(idiomaManager.get("FINAIS_VAZIOS"));
        } else {
            for (String fim : dadosGlobais.getFinais()) {
                view.exibirMensagem(" * " + traduzirFinalRegistrado(fim));
            }
        }
        view.pausarParaLeitura(idiomaManager);
    }

    /**
     * Converte o registro de um final alcançado para o título equivalente no idioma ativo.
     */
    private String traduzirFinalRegistrado(String fimRegistrado) {
        if (fimRegistrado == null) return "";
        int idCenaFinal = -1;
        if (fimRegistrado.contains(" 1 -")) idCenaFinal = 201;
        else if (fimRegistrado.contains(" 2 -")) idCenaFinal = 21;
        else if (fimRegistrado.contains(" 3 -")) idCenaFinal = 22;
        else if (fimRegistrado.contains(" 4 -")) idCenaFinal = 23;

        if (idCenaFinal != -1) {
            CenaBase cena = buscarCenaPorId(idCenaFinal);
            if (cena != null && !cena.getTitulo().isEmpty()) {
                return idiomaManager.get("PREFIXO_FINAL") + cena.getTitulo();
            }
        }
        return fimRegistrado;
    }

    /**
     * Limpa a tela e imprime o resumo visual atual dos 3 slots de salvamento.
     */
    private void exibirListaDeSlots(String titulo) {
        view.limparTela();
        view.exibirMensagem("=== " + titulo + " ===");
        for (int i = 1; i <= 3; i++) view.exibirMensagem(saveCtrl.obterResumoVisualSlot(i));
    }

    /**
     * Solicita uma confirmação do tipo Sim/Não (S/N ou Y/N) ao usuário.
     * Aceita tanto "S" (Português/Espanhol) quanto "Y" (Inglês).
     */
    private boolean confirmarAcao(String pergunta) {
        view.exibirMensagem(pergunta + " " + idiomaManager.get("CONFIRMACAO_SN"));
        String resp = view.lerEntrada().trim();
        return resp.equalsIgnoreCase("S") || resp.equalsIgnoreCase("Y");
    }

    /**
     * Busca uma cena no mapa carregado do roteiro a partir de seu identificador numérico.
     */
    private CenaBase buscarCenaPorId(int id) {
        return (roteiroCenas == null) ? null : roteiroCenas.get(id);
    }

    /**
     * Lê a entrada do usuário e tenta convertê-la para um número inteiro, retornando -1 se inválida.
     */
    private int lerOpcaoNumerica() {
        try {
            return Integer.parseInt(view.lerEntrada().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
