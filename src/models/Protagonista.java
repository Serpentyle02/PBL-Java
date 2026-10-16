package models;

import java.util.HashMap;
import java.util.Map;

public class Protagonista extends Personagem {

    private int estresse = 0;
    private int coragem = 0;
    private int logica = 0;
    private int carisma = 0;
    private int pistas = 0;
    private int reputacao = 0;

    private final Map<String, Integer> relacionamentos = new HashMap<>();

    public Protagonista(String nome) {
        super(nome);
    }

    public void atualizarRelacionamento(String nomeNpc, int delta) {
        if (nomeNpc == null || nomeNpc.trim().isEmpty() || delta == 0) return;
        String chave = nomeNpc.trim();
        relacionamentos.put(chave, relacionamentos.getOrDefault(chave, 0) + delta);
    }

    public int getEstresse() { return estresse; }
    public void setEstresse(int v) { this.estresse = Math.max(0, v); }

    public int getCoragem() { return coragem; }
    public void setCoragem(int v) { this.coragem = Math.max(0, v); }

    public int getLogica() { return logica; }
    public void setLogica(int v) { this.logica = Math.max(0, v); }

    public int getCarisma() { return carisma; }
    public void setCarisma(int v) { this.carisma = Math.max(0, v); }

    public int getPistas() { return pistas; }
    public void setPistas(int v) { this.pistas = Math.max(0, v); }

    public int getReputacao() { return reputacao; }
    public void setReputacao(int v) { this.reputacao = Math.max(0, v); }

    public Map<String, Integer> getRelacionamentos() { return relacionamentos; }
}