import java.util.HashMap;
import java.util.Map;

public class Protagonista extends Personagem {
    private int logica, carisma, estresse, coragem, pistas;
    private int reputacaoGlobal; 
    private Map<String, Integer> rels; 

    public Protagonista(String nome) {
        super(nome);
        this.rels = new HashMap<>();
        this.reputacaoGlobal = 0;
    }

    public void definirAtributosIniciais(int logica, int carisma, int estresse, int coragem) {
        this.logica = Math.max(0, logica);
        this.carisma = Math.max(0, carisma);
        this.estresse = Math.max(0, estresse);
        this.coragem = Math.max(0, coragem);
        this.pistas = 0; 
    }

    // Trava do piso mínimo em 0 para todos os atributos
    public void alterarLogica(int valor) { 
        this.logica = Math.max(0, this.logica + valor); 
    }
    
    public void alterarCarisma(int valor) { 
        this.carisma = Math.max(0, this.carisma + valor); 
    }
    
    public void alterarEstresse(int valor) { 
        this.estresse = Math.max(0, this.estresse + valor); 
    }
    
    public void alterarCoragem(int valor) { 
        this.coragem = Math.max(0, this.coragem + valor); 
    }
    
    public void alterarPistas(int valor) { 
        this.pistas = Math.max(0, this.pistas + valor); 
    }
    
    public void alterarReputacaoGlobal(int valor) { 
        this.reputacaoGlobal = Math.max(0, this.reputacaoGlobal + valor); 
    }

    public void alterarRelacionamento(NPC n, int v) {
        int atual = getRelacionamento(n);
        rels.put(n.getNome(), Math.max(0, atual + v));
    }

    public int getRelacionamento(NPC n) { return rels.getOrDefault(n.getNome(), 0); }
    public int getReputacaoGlobal() { return reputacaoGlobal; }
    public int getPistas() { return pistas; }
    public int getLogica() { return logica; }
    public int getCarisma() { return carisma; }
    public int getEstresse() { return estresse; }
    public int getCoragem() { return coragem; }
}