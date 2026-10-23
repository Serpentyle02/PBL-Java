package models;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa o personagem controlado pelo jogador (investigador protagonista).
 * Gerencia seus atributos primários (Lógica, Carisma, Estresse, Coragem, Pistas e Reputação)
 * com trava numérica em zero, além do mapa de afinidade com os NPCs da história.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class Protagonista extends Personagem {

    // Atributos numéricos do protagonista (nunca assumem valores negativos)
    private int estresse = 0;
    private int coragem = 0;
    private int logica = 0;
    private int carisma = 0;
    private int pistas = 0;
    private int reputacao = 0;

    // Mapa que armazena o nível de relacionamento/afinidade com cada NPC (Nome do NPC -> Pontuação)
    private final Map<String, Integer> relacionamentos = new HashMap<>();

    /**
     * Construtor do Protagonista.
     *
     * @param nome Nome escolhido pelo jogador no início da partida.
     */
    public Protagonista(String nome) {
        super(nome); // Repassa o nome para a superclasse Personagem
    }

    /**
     * Atualiza o nível de afinidade do protagonista com um determinado NPC.
     * Caso o NPC ainda não esteja registrado no mapa, inicia sua afinidade a partir de 0.
     *
     * @param nomeNpc Nome do NPC afetado pela escolha.
     * @param delta   Valor a ser somado (positivo) ou subtraído (negativo) na relação.
     */
    public void atualizarRelacionamento(String nomeNpc, int delta) {
        if (nomeNpc == null || nomeNpc.trim().isEmpty() || delta == 0) return;
        String chave = nomeNpc.trim();
        relacionamentos.put(chave, relacionamentos.getOrDefault(chave, 0) + delta);
    }

    /**
     * Atualiza a afinidade do protagonista com um {@link NPC}.
     *
     * @param npc   NPC afetado pela escolha (ignorado se nulo).
     * @param delta Valor a ser somado ou subtraído na relação.
     */
    public void atualizarRelacionamento(NPC npc, int delta) {
        if (npc == null) return;
        atualizarRelacionamento(npc.getNome(), delta);
    }

    /**
     * Consulta o nível de afinidade atual com um NPC.
     *
     * @param npc NPC consultado.
     * @return Afinidade acumulada (0 se ainda não houve interação).
     */
    public int getRelacionamento(NPC npc) {
        return (npc == null) ? 0 : relacionamentos.getOrDefault(npc.getNome(), 0);
    }

    // =========================================================================
    // GETTERS E SETTERS COM TRAVA NUMÉRICA (Math.max garante piso mínimo em 0)
    // =========================================================================

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

    /**
     * Obtém o mapa completo de relacionamentos do protagonista com os NPCs.
     *
     * @return Map associando o nome de cada NPC ao seu valor de afinidade.
     */
    public Map<String, Integer> getRelacionamentos() { return relacionamentos; }
}
