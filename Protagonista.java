import java.util.HashMap;
import java.util.Map;

public class Protagonista extends Personagem {
    private int logica, carisma, estresse, pistas;
    private int reputacaoGlobal; // Adição (Relatório 08/09)
    private Map<String, Integer> rels; 

    public Protagonista(String nome) {
        super(nome);
        this.rels = new HashMap<>();
        this.reputacaoGlobal = 0;
    }

    // Método para o novo sistema de pontos (Relatório 18/08)
    public void definirAtributosIniciais(int logica, int carisma, int estresse) {
        this.logica = logica;
        this.carisma = carisma;
        this.estresse = estresse;
        this.pistas = 0; // Pistas começam zeradas
    }

    public void alterarLogica(int valor) { this.logica += valor; }
    public void alterarCarisma(int valor) { this.carisma += valor; }
    public void alterarEstresse(int valor) { this.estresse += valor; }
    public void alterarPistas(int valor) { this.pistas += valor; }
    public void alterarReputacaoGlobal(int valor) { this.reputacaoGlobal += valor; }

    public void alterarRelacionamento(NPC n, int v) {
        int atual = getRelacionamento(n);
        rels.put(n.getNome(), atual + v);
    }

    public int getRelacionamento(NPC n) { return rels.getOrDefault(n.getNome(), 0); }
    public int getReputacaoGlobal() { return reputacaoGlobal; }
    public int getPistas() { return pistas; }
    public int getLogica() { return logica; }
    public int getCarisma() { return carisma; }
    public int getEstresse() { return estresse; }
}