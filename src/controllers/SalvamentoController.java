package controllers;

import models.Protagonista;
import models.SalvamentoException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class SalvamentoController {

    /**
     * Define onde fica o savegame.json, usando a pasta JsonFiles que existir.
     */
    public Path caminhoSave() {
        String[] bases = {"src/JsonFiles", "JsonFiles"};
        for (String base : bases) {
            if (Files.isDirectory(Paths.get(base))) {
                return Paths.get(base, "savegame.json");
            }
        }
        return Paths.get("src/JsonFiles", "savegame.json");
    }

    public void salvarProgresso(Protagonista jogador, int idCenaAtual) throws SalvamentoException {

        if (jogador == null) {
            throw new SalvamentoException("Falha no salvamento: O objeto Protagonista está nulo.");
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"idCenaAtual\": ").append(idCenaAtual).append(",\n");
        json.append("  \"jogador\": {\n");
        json.append("    \"nome\": \"").append(escapar(jogador.getNome())).append("\",\n");
        json.append("    \"logica\": ").append(jogador.getLogica()).append(",\n");
        json.append("    \"carisma\": ").append(jogador.getCarisma()).append(",\n");
        json.append("    \"estresse\": ").append(jogador.getEstresse()).append(",\n");
        json.append("    \"coragem\": ").append(jogador.getCoragem()).append(",\n");
        json.append("    \"pistas\": ").append(jogador.getPistas()).append(",\n");
        json.append("    \"reputacao\": ").append(jogador.getReputacaoGlobal()).append(",\n");
        json.append("    \"relacionamentos\": {");

        boolean primeiro = true;
        for (Map.Entry<String, Integer> e : jogador.getRelacionamentos().entrySet()) {
            if (!primeiro) json.append(",");
            json.append("\n      \"").append(escapar(e.getKey())).append("\": ").append(e.getValue());
            primeiro = false;
        }
        json.append(primeiro ? "}\n" : "\n    }\n");

        json.append("  }\n");
        json.append("}");

        Path arquivo = caminhoSave();
        try {
            Files.createDirectories(arquivo.getParent());
            Files.write(arquivo, json.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new SalvamentoException("Erro de I/O ao tentar gravar o arquivo em " + arquivo, e);
        }
    }

    /**
     * Protege aspas e barras no nome para não quebrar o JSON.
     */
    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}