/**
 * Subclasse que representa um Personagem Não Jogável (NPC) no Model.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class NPC extends Personagem {
    // Papel ou função do NPC na narrativa (ex: "Aliada", "Rival", "Orientador")
    private String papelNaHistoria;
    
    /**
     * Construtor da classe NPC.
     * @param nome Nome do NPC.
     * @param papel Função desempenhada na história.
     */
    public NPC(String nome, String papel) {
        super(nome); // Inicializa o nome herdado de Personagem
        this.papelNaHistoria = papel; // Define a função narrativa
    }
}