package models;

import java.util.HashMap;
import java.util.Map;

/**
 * Subclasse que representa um Personagem Não Jogável (NPC) no Model.
 * Herda o atributo nome da superclasse abstrata Personagem e mantém um
 * catálogo único (uma instância por nome) dos NPCs da história.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class NPC extends Personagem {

    // Catálogo de NPCs já criados, indexado pelo nome (garante uma única instância por personagem)
    private static final Map<String, NPC> CATALOGO = new HashMap<>();

    // Papel ou função do NPC na narrativa (ex: "Aliada", "Rival", "Orientador")
    private String papelNaHistoria;

    /**
     * Construtor da classe NPC.
     *
     * @param nome  Nome do NPC.
     * @param papel Função desempenhada na história.
     */
    public NPC(String nome, String papel) {
        super(nome); // Inicializa o nome herdado de Personagem
        this.papelNaHistoria = papel; // Define a função narrativa
    }

    /**
     * Obtém a instância do NPC com o nome informado, criando-a no primeiro acesso.
     * NPCs conhecidos da história recebem seu papel narrativo; os demais recebem um papel genérico.
     *
     * @param nome Nome do NPC (como aparece no roteiro).
     * @return Instância única de {@link NPC} para aquele nome, ou {@code null} se o nome for vazio.
     */
    public static synchronized NPC obter(String nome) {
        if (nome == null || nome.trim().isEmpty()) return null;
        String chave = nome.trim();
        NPC npc = CATALOGO.get(chave);
        if (npc == null) {
            npc = new NPC(chave, papelPadrao(chave));
            CATALOGO.put(chave, npc);
        }
        return npc;
    }

    /**
     * Define o papel narrativo dos NPCs conhecidos do roteiro.
     */
    private static String papelPadrao(String nome) {
        switch (nome) {
            case "Elisa":       return "Aliada";
            case "Prof. Silva": return "Orientador";
            case "Beto":        return "Rival";
            case "Luiza":       return "Hacker";
            default:            return "Personagem secundário";
        }
    }

    /**
     * Obtém o papel desempenhado pelo NPC na narrativa.
     *
     * @return String com o papel na história.
     */
    public String getPapelNaHistoria() {
        return papelNaHistoria;
    }
}
