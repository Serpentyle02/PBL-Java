package controllers;

import models.ProgressoJogo;
import models.Protagonista;
import models.SalvamentoException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controlador responsável pela persistência de dados do jogo em arquivos JSON (codificação UTF-8).
 * Gerencia o salvamento, carregamento e exclusão dos 3 slots de partida ({@code save_slot_1..3.json}),
 * bem como a leitura e gravação das preferências (incluindo idioma), conquistas e finais em {@code dados_globais.json}.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class SalvamentoController {

    // Diretório padrão onde os arquivos JSON de save e roteiro ficam armazenados
    private static final String PASTA_PADRAO = "src/jsonFiles";
    // Formato de data e hora exibido nos resumos dos slots de salvamento
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Diretório efetivo dos arquivos de save e arquivo de dados globais (preferências, galeria e finais)
    private final String pasta;
    private final String arquivoGlobal;

    /**
     * Cria o controlador usando a pasta padrão do projeto ({@code src/jsonFiles}).
     */
    public SalvamentoController() {
        this(PASTA_PADRAO);
    }

    /**
     * Cria o controlador gravando os arquivos em uma pasta específica (útil para testes automatizados).
     *
     * @param pasta Diretório onde os saves e os dados globais serão gravados.
     */
    public SalvamentoController(String pasta) {
        this.pasta = pasta;
        this.arquivoGlobal = pasta + "/dados_globais.json";
    }

    /**
     * Grava o estado completo da partida atual (protagonista, todos os atributos, reputação,
     * relacionamentos, cena atual, título do capítulo e resumo) no arquivo correspondente ao slot informado (1 a 3).
     *
     * @param slot       Número do slot escolhido (1, 2 ou 3).
     * @param p          Instância atual do Protagonista.
     * @param idCena     ID da cena em que o jogador se encontra.
     * @param tituloCena Título do capítulo/cena atual (exibido nos metadados do save).
     * @param resumo     Breve trecho da cena para exibição na listagem de slots.
     * @throws SalvamentoException Caso o slot seja inválido, o protagonista seja nulo ou ocorra erro de I/O.
     */
    public void salvarSlot(int slot, Protagonista p, int idCena, String tituloCena, String resumo) throws SalvamentoException {
        validarSlot(slot);
        if (p == null) throw new SalvamentoException("Protagonista inválido (nulo).");

        StringBuilder json = new StringBuilder("{\n");
        campoTexto(json, "nome", p.getNome());
        campoTexto(json, "dataHora", LocalDateTime.now().format(FORMATO_DATA));
        campoTexto(json, "tituloCena", tituloCena);
        campoTexto(json, "resumo", resumo);
        campoNumero(json, "idCenaAtual", idCena);
        campoNumero(json, "logica", p.getLogica());
        campoNumero(json, "carisma", p.getCarisma());
        campoNumero(json, "estresse", p.getEstresse());
        campoNumero(json, "coragem", p.getCoragem());
        campoNumero(json, "pistas", p.getPistas());
        campoNumero(json, "reputacao", p.getReputacao());

        // Serializa o mapa de relacionamentos com NPCs como um subobjeto JSON
        json.append("  \"relacionamentos\": {");
        int i = 0;
        for (Map.Entry<String, Integer> e : p.getRelacionamentos().entrySet()) {
            if (i++ > 0) json.append(", ");
            json.append("\"").append(escapar(e.getKey())).append("\": ").append(e.getValue());
        }
        json.append("}\n}");

        escreverArquivo(caminhoDoSlot(slot), json.toString());
    }

    /**
     * Lê o arquivo de um slot de salvamento (1 a 3) e reconstrói o objeto {@link ProgressoJogo}
     * contendo o protagonista com todos os seus atributos e relacionamentos restaurados.
     *
     * @param slot Número do slot a ser carregado (1, 2 ou 3).
     * @return Instância de {@link ProgressoJogo} com os dados da partida salva.
     * @throws SalvamentoException Caso o slot seja inválido, esteja vazio ou o arquivo não possa ser lido.
     */
    public ProgressoJogo carregarSlot(int slot) throws SalvamentoException {
        validarSlot(slot);
        if (!existeSlot(slot)) throw new SalvamentoException("O Slot " + slot + " está vazio.");

        String json = lerArquivo(caminhoDoSlot(slot));

        Protagonista p = new Protagonista(extrairTexto(json, "nome", "Investigador"));
        p.setLogica(extrairInteiro(json, "logica", 0));
        p.setCarisma(extrairInteiro(json, "carisma", 0));
        p.setEstresse(extrairInteiro(json, "estresse", 0));
        p.setCoragem(extrairInteiro(json, "coragem", 0));
        p.setPistas(extrairInteiro(json, "pistas", 0));
        p.setReputacao(extrairInteiro(json, "reputacao", 0));

        // Reconstrói o mapa de relacionamentos a partir do bloco "relacionamentos": { ... }
        Matcher mRel = Pattern.compile("\"relacionamentos\"\\s*:\\s*\\{([^}]*)\\}").matcher(json);
        if (mRel.find()) {
            Matcher mItem = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"\\s*:\\s*(-?\\d+)").matcher(mRel.group(1));
            while (mItem.find()) {
                p.atualizarRelacionamento(desescapar(mItem.group(1)), Integer.parseInt(mItem.group(2)));
            }
        }

        return new ProgressoJogo(slot, p,
                extrairInteiro(json, "idCenaAtual", 1),
                extrairTexto(json, "tituloCena", "-"),
                extrairTexto(json, "dataHora", "--/--/----"),
                extrairTexto(json, "resumo", "Sem resumo"));
    }

    /**
     * Remove permanentemente o arquivo JSON correspondente ao slot informado.
     *
     * @param slot Número do slot a ser excluído (1, 2 ou 3).
     * @throws SalvamentoException Caso o slot seja inválido ou ocorra falha ao remover o arquivo.
     */
    public void excluirSlot(int slot) throws SalvamentoException {
        validarSlot(slot);
        try {
            Files.deleteIfExists(Paths.get(caminhoDoSlot(slot)));
        } catch (IOException e) {
            throw new SalvamentoException("Não foi possível excluir o Slot " + slot);
        }
    }

    /**
     * Verifica se já existe um arquivo de save gravado para o slot informado.
     *
     * @param slot Número do slot (1 a 3).
     * @return {@code true} se o arquivo existir; {@code false} caso contrário.
     */
    public boolean existeSlot(int slot) {
        return Files.exists(Paths.get(caminhoDoSlot(slot)));
    }

    /**
     * Descobre qual slot foi gravado mais recentemente (usado pela opção "Continuar" do menu).
     * A comparação usa a data de modificação dos arquivos de save.
     *
     * @return Número do slot (1 a 3) mais recente, ou 0 se não houver nenhum save.
     */
    public int obterSlotMaisRecente() {
        int melhor = 0;
        long melhorData = Long.MIN_VALUE;
        for (int i = 1; i <= 3; i++) {
            if (!existeSlot(i)) continue;
            try {
                long data = Files.getLastModifiedTime(Paths.get(caminhoDoSlot(i))).toMillis();
                if (data > melhorData) {
                    melhorData = data;
                    melhor = i;
                }
            } catch (IOException ignored) {
                // Slot ilegível é ignorado na busca
            }
        }
        return melhor;
    }

    /**
     * Gera uma linha formatada com os metadados do slot: nome do jogador, capítulo (título da cena),
     * data e hora e resumo da progressão. Não exibe o ID numérico interno da cena.
     *
     * @param slot Número do slot (1 a 3).
     * @return String descritiva do estado do slot (preenchido, vazio ou corrompido).
     */
    public String obterResumoVisualSlot(int slot) {
        if (!existeSlot(slot)) return "[Slot " + slot + "] --- VAZIO ---";
        try {
            ProgressoJogo prog = carregarSlot(slot);
            return String.format("[Slot %d] %s | Cap.: %s | %s | (%s)",
                    slot, prog.getProtagonista().getNome(), prog.getTituloCena(),
                    prog.getDataHora(), prog.getResumoProgressao());
        } catch (SalvamentoException e) {
            return "[Slot " + slot + "] --- CORROMPIDO ---";
        }
    }

    /**
     * Persiste as preferências do usuário (incluindo idioma), conquistas da galeria e finais descobertos
     * no arquivo {@code dados_globais.json}.
     *
     * @param global Objeto {@link ProgressoJogo} contendo os dados globais.
     * @throws SalvamentoException Caso ocorra falha de gravação no disco.
     */
    public void salvarDadosGlobais(ProgressoJogo global) throws SalvamentoException {
        String json = "{\n"
                + "  \"velocidadeTextoMs\": " + global.getVelocidadeTextoMs() + ",\n"
                + "  \"autoSaveAtivo\": " + global.isAutoSaveAtivo() + ",\n"
                + "  \"exibirDicas\": " + global.isExibirDicas() + ",\n"
                + "  \"idiomaPref\": \"" + escapar(global.getIdiomaPref()) + "\",\n"
                + "  \"desbloqueaveis\": " + formatarListaJson(global.getDesbloqueaveis()) + ",\n"
                + "  \"finais\": " + formatarListaJson(global.getFinais()) + "\n"
                + "}";
        escreverArquivo(arquivoGlobal, json);
    }

    /**
     * Carrega as preferências, idioma, conquistas e finais registrados em {@code dados_globais.json}.
     * Se o arquivo ainda não existir, devolve uma instância com os valores padrão.
     *
     * @return Instância de {@link ProgressoJogo} com os dados globais.
     */
    public ProgressoJogo carregarDadosGlobais() {
        ProgressoJogo global = new ProgressoJogo();
        if (!Files.exists(Paths.get(arquivoGlobal))) return global;
        try {
            String json = lerArquivo(arquivoGlobal);
            global.setVelocidadeTextoMs(extrairInteiro(json, "velocidadeTextoMs", 20));
            global.setAutoSaveAtivo(extrairBooleano(json, "autoSaveAtivo", true));
            global.setExibirDicas(extrairBooleano(json, "exibirDicas", true));
            global.setIdiomaPref(extrairTexto(json, "idiomaPref", "pt_BR"));
            extrairLista(json, "desbloqueaveis", global.getDesbloqueaveis());
            extrairLista(json, "finais", global.getFinais());
        } catch (SalvamentoException ignored) {
            // Retorna valores padrão se o arquivo estiver inacessível
        }
        return global;
    }

    // =========================================================================
    // MÉTODOS AUXILIARES PRIVADOS DE I/O E SERIALIZAÇÃO/PARSING JSON
    // =========================================================================

    private String caminhoDoSlot(int slot) {
        return pasta + "/save_slot_" + slot + ".json";
    }

    private void validarSlot(int slot) throws SalvamentoException {
        if (slot < 1 || slot > 3) throw new SalvamentoException("Slot inválido! Escolha entre 1, 2 ou 3.");
    }

    private void escreverArquivo(String caminho, String conteudo) throws SalvamentoException {
        try {
            Path path = Paths.get(caminho);
            Files.createDirectories(path.getParent());
            Files.write(path, conteudo.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new SalvamentoException("Falha de gravação em '" + caminho + "': " + e.getMessage());
        }
    }

    private String lerArquivo(String caminho) throws SalvamentoException {
        try {
            return new String(Files.readAllBytes(Paths.get(caminho)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SalvamentoException("Falha de leitura em '" + caminho + "': " + e.getMessage());
        }
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }

    private String desescapar(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private void campoTexto(StringBuilder sb, String chave, String valor) {
        sb.append("  \"").append(chave).append("\": \"").append(escapar(valor)).append("\",\n");
    }

    private void campoNumero(StringBuilder sb, String chave, int valor) {
        sb.append("  \"").append(chave).append("\": ").append(valor).append(",\n");
    }

    private String formatarListaJson(List<String> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(escapar(lista.get(i))).append("\"");
        }
        return sb.append("]").toString();
    }

    private String extrairTexto(String json, String chave, String padrao) {
        Matcher m = Pattern.compile("\"" + chave + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(json);
        return m.find() ? desescapar(m.group(1)) : padrao;
    }

    private int extrairInteiro(String json, String chave, int padrao) {
        Matcher m = Pattern.compile("\"" + chave + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : padrao;
    }

    private boolean extrairBooleano(String json, String chave, boolean padrao) {
        Matcher m = Pattern.compile("\"" + chave + "\"\\s*:\\s*(true|false)").matcher(json);
        return m.find() ? Boolean.parseBoolean(m.group(1)) : padrao;
    }

    private void extrairLista(String json, String chave, List<String> destino) {
        Matcher mBloco = Pattern.compile("\"" + chave + "\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
        if (mBloco.find()) {
            Matcher mItem = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"").matcher(mBloco.group(1));
            while (mItem.find()) destino.add(desescapar(mItem.group(1)));
        }
    }
}
