import java.util.HashMap;
import java.util.Map;

/**
 * Representa o jogador principal no padrão Model.
 * Herda da classe Personagem e gerencia atributos, recursos e afinidades com NPCs.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class Protagonista extends Personagem {
    // Atributos numéricos de capacidade do jogador
    private int logica, carisma, estresse, coragem, pistas;
    // Reputação global no meio acadêmico
    private int reputacaoGlobal; 
    // Mapeamento dinâmico do grau de afinidade/relacionamento com cada NPC
    private Map<String, Integer> rels; 

    /**
     * Construtor do Protagonista.
     * @param nome Nome do jogador.
     */
    public Protagonista(String nome) {
        super(nome); // Chama o construtor da classe pai (Personagem)
        this.rels = new HashMap<>(); // Instancia o mapa de relacionamentos
        this.reputacaoGlobal = 0; // Inicializa a reputação neutra
    }

    /**
     * Define a alocação de atributos iniciais garantindo limite mínimo de 0.
     */
    public void definirAtributosIniciais(int logica, int carisma, int estresse, int coragem) {
        this.logica = Math.max(0, logica); // Garante que lógica >= 0
        this.carisma = Math.max(0, carisma); // Garante que carisma >= 0
        this.estresse = Math.max(0, estresse); // Garante que estresse >= 0
        this.coragem = Math.max(0, coragem); // Garante que coragem >= 0
        this.pistas = 0; // Inicia sem pistas coletadas
    }

    // Métodos para alteração com trava em piso zero (Math.max(0, ...))

    public void alterarLogica(int valor) { 
        this.logica = Math.max(0, this.logica + valor); // Atualiza lógica impedindo valor negativo
    }
    
    public void alterarCarisma(int valor) { 
        this.carisma = Math.max(0, this.carisma + valor); // Atualiza carisma impedindo valor negativo
    }
    
    public void alterarEstresse(int valor) { 
        this.estresse = Math.max(0, this.estresse + valor); // Atualiza estresse impedindo valor negativo
    }
    
    public void alterarCoragem(int valor) { 
        this.coragem = Math.max(0, this.coragem + valor); // Atualiza coragem impedindo valor negativo
    }
    
    public void alterarPistas(int valor) { 
        this.pistas = Math.max(0, this.pistas + valor); // Atualiza pistas impedindo valor negativo
    }
    
    public void alterarReputacaoGlobal(int valor) { 
        this.reputacaoGlobal = Math.max(0, this.reputacaoGlobal + valor); // Atualiza reputação com piso zero
    }

    /**
     * Atualiza o nível de relacionamento com um NPC.
     */
    public void alterarRelacionamento(NPC n, int v) {
        int atual = getRelacionamento(n); // Obtém a afinidade atual
        rels.put(n.getNome(), Math.max(0, atual + v)); // Salva no mapa garantindo piso zero
    }

    // Métodos seletores (Getters) dos atributos do jogador
    public int getRelacionamento(NPC n) { return rels.getOrDefault(n.getNome(), 0); }
    public int getReputacaoGlobal() { return reputacaoGlobal; }
    public int getPistas() { return pistas; }
    public int getLogica() { return logica; }
    public int getCarisma() { return carisma; }
    public int getEstresse() { return estresse; }
    public int getCoragem() { return coragem; }
}