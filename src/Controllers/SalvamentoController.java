package controllers;

import models.Protagonista;
import models.SalvamentoException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SalvamentoController {

    public void salvarProgresso(Protagonista jogador, int idCenaAtual) throws SalvamentoException {
        
        if (jogador == null) {
            throw new SalvamentoException("Falha no salvamento: O objeto Protagonista está nulo.");
        }

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"idCenaAtual\": ").append(idCenaAtual).append(",\n");
        jsonBuilder.append("  \"jogador\": {\n");
        jsonBuilder.append("    \"logica\": ").append(jogador.getLogica()).append(",\n");
        jsonBuilder.append("    \"carisma\": ").append(jogador.getCarisma()).append(",\n");
        jsonBuilder.append("    \"estresse\": ").append(jogador.getEstresse()).append(",\n");
        jsonBuilder.append("    \"coragem\": ").append(jogador.getCoragem()).append(",\n");
        jsonBuilder.append("    \"pistas\": ").append(jogador.getPistas()).append(",\n");
        jsonBuilder.append("    \"reputacao\": ").append(jogador.getReputacao()).append("\n");
        jsonBuilder.append("  }\n");
        jsonBuilder.append("}");

        // Verificação de segurança: Cria a pasta JsonFiles caso ela não exista
        File diretorio = new File("src/JsonFiles");
        if (!diretorio.exists()) {
            diretorio.mkdirs(); 
        }

        // Gravação apontando para o caminho correto
        try (FileWriter fileWriter = new FileWriter("src/JsonFiles/savegame.json")) {
            fileWriter.write(jsonBuilder.toString());
        } catch (IOException e) {
            throw new SalvamentoException("Erro de I/O ao tentar gravar o arquivo em src/JsonFiles/savegame.json.", e);
        }
    }
}