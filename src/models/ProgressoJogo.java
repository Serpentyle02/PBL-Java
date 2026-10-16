package models;

import java.util.ArrayList;
import java.util.List;

public class ProgressoJogo {

    private int slotId;
    private int idCenaAtual;
    private String dataHora;
    private String resumoProgressao;
    private Protagonista protagonista;

    private int velocidadeTextoMs = 20;
    private boolean autoSaveAtivo = true;
    private boolean exibirDicas = true;
    private final List<String> desbloqueaveis = new ArrayList<>();
    private final List<String> finais = new ArrayList<>();

    public ProgressoJogo(int slotId, Protagonista protagonista, int idCenaAtual, String dataHora, String resumo) {
        this.slotId = slotId;
        this.protagonista = protagonista;
        this.idCenaAtual = idCenaAtual;
        this.dataHora = dataHora;
        this.resumoProgressao = resumo;
    }

    public ProgressoJogo() {}

    public boolean adicionarDesbloqueavel(String item) {
        return !desbloqueaveis.contains(item) && desbloqueaveis.add(item);
    }

    public boolean adicionarFinal(String tituloFinal) {
        return !finais.contains(tituloFinal) && finais.add(tituloFinal);
    }

    public int getSlotId() { return slotId; }
    public int getIdCenaAtual() { return idCenaAtual; }
    public String getDataHora() { return dataHora; }
    public String getResumoProgressao() { return resumoProgressao; }
    public Protagonista getProtagonista() { return protagonista; }

    public int getVelocidadeTextoMs() { return velocidadeTextoMs; }
    public void setVelocidadeTextoMs(int v) { this.velocidadeTextoMs = v; }

    public boolean isAutoSaveAtivo() { return autoSaveAtivo; }
    public void setAutoSaveAtivo(boolean v) { this.autoSaveAtivo = v; }

    public boolean isExibirDicas() { return exibirDicas; }
    public void setExibirDicas(boolean v) { this.exibirDicas = v; }

    public List<String> getDesbloqueaveis() { return desbloqueaveis; }
    public List<String> getFinais() { return finais; }
}