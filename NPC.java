public class NPC extends Personagem {
    private String papelNaHistoria;
    
    public NPC(String nome, String papel) {
        super(nome);
        this.papelNaHistoria = papel;
    }
}