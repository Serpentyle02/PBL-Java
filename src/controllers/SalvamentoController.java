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

public class SalvamentoController {

    private static final String PASTA = "src/jsonFiles";
    private static final String ARQUIVO_GLOBAL = PASTA + "/dados_globais.json";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void salvarSlot(int slot, Protagonista p, int idCena, String resumo) throws SalvamentoException {
        validarSlot(slot);
        if (p == null) throw new SalvamentoException("Protagonista inválido (nulo).");

        StringBuilder json = new StringBuilder("{\n");
        campoTexto(json, "nome", p.getNome());
        campoTexto(json, "dataHora", LocalDateTime.now().format(FORMATO_DATA));
        campoTexto(json, "resumo", resumo);
        campoNumero(json, "idCenaAtual", idCena);
        campoNumero(json, "logica", p.getLogica());
        campoNumero(json, "carisma", p.getCarisma());
        campoNumero(json, "estresse", p.getEstresse());
        campoNumero(json, "coragem", p.getCoragem());
        campoNumero(json, "pistas", p.getPistas());
        campoNumero(json, "reputacao", p.getReputacao());

        json.append("  \"relacionamentos\": {");
        int i = 0;
        for (Map.Entry<String, Integer> e : p.getRelacionamentos().entrySet()) {
            if (i++ > 0) json.append(", ");
            json.append("\"").append(escapar(e.getKey())).append("\": ").append(e.getValue());
        }
        json.append("}\n}");

        escreverArquivo(caminhoDoSlot(slot), json.toString());
    }

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

        Matcher mRel = Pattern.compile("\"relacionamentos\"\\s*:\\s*\\{([^}]*)\\}").matcher(json);
        if (mRel.find()) {
            Matcher mItem = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"\\s*:\\s*(-?\\d+)").matcher(mRel.group(1));
            while (mItem.find()) {
                p.atualizarRelacionamento(desescapar(mItem.group(1)), Integer.parseInt(mItem.group(2)));
            }
        }

        return new ProgressoJogo(slot, p,
                extrairInteiro(json, "idCenaAtual", 1),
                extrairTexto(json, "dataHora", "--/--/----"),
                extrairTexto(json, "resumo", "Sem resumo"));
    }

    public void excluirSlot(int slot) throws SalvamentoException {
        validarSlot(slot);
        try {
            Files.deleteIfExists(Paths.get(caminhoDoSlot(slot)));
        } catch (IOException e) {
            throw new SalvamentoException("Não foi possível excluir o Slot " + slot);
        }
    }

    public boolean existeSlot(int slot) {
        return Files.exists(Paths.get(caminhoDoSlot(slot)));
    }

    public String obterResumoVisualSlot(int slot) {
        if (!existeSlot(slot)) return "[Slot " + slot + "] --- VAZIO ---";
        try {
            ProgressoJogo prog = carregarSlot(slot);
            return String.format("[Slot %d] %s | %s | (%s)",
                    slot, prog.getProtagonista().getNome(),
                    prog.getDataHora(), prog.getResumoProgressao());
        } catch (SalvamentoException e) {
            return "[Slot " + slot + "] --- CORROMPIDO ---";
        }
    }

    public void salvarDadosGlobais(ProgressoJogo global) throws SalvamentoException {
        String json = "{\n"
                + "  \"velocidadeTextoMs\": " + global.getVelocidadeTextoMs() + ",\n"
                + "  \"autoSaveAtivo\": " + global.isAutoSaveAtivo() + ",\n"
                + "  \"exibirDicas\": " + global.isExibirDicas() + ",\n"
                + "  \"desbloqueaveis\": " + formatarListaJson(global.getDesbloqueaveis()) + ",\n"
                + "  \"finais\": " + formatarListaJson(global.getFinais()) + "\n"
                + "}";
        escreverArquivo(ARQUIVO_GLOBAL, json);
    }

    public ProgressoJogo carregarDadosGlobais() {
        ProgressoJogo global = new ProgressoJogo();
        if (!Files.exists(Paths.get(ARQUIVO_GLOBAL))) return global;
        try {
            String json = lerArquivo(ARQUIVO_GLOBAL);
            global.setVelocidadeTextoMs(extrairInteiro(json, "velocidadeTextoMs", 20));
            global.setAutoSaveAtivo(extrairBooleano(json, "autoSaveAtivo", true));
            global.setExibirDicas(extrairBooleano(json, "exibirDicas", true));
            extrairLista(json, "desbloqueaveis", global.getDesbloqueaveis());
            extrairLista(json, "finais", global.getFinais());
        } catch (SalvamentoException ignored) {}
        return global;
    }

    private String caminhoDoSlot(int slot) {
        return PASTA + "/save_slot_" + slot + ".json";
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