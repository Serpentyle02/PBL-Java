import java.util.ArrayList;
import java.util.List;

public abstract class CenaBase {
    protected int id;
    protected String titulo;
    protected String textoNarrativo;
    protected boolean isFinal;
    protected List<Opcao> opcoes = new ArrayList<>();

    // Método polimórfico
    public abstract void carregarDados(String dados);

    public void adicionarOpcao(Opcao o) { this.opcoes.add(o); }

    public List<Opcao> getOpcoes(Protagonista p) {
        List<Opcao> validas = new ArrayList<>();
        for (Opcao o : opcoes) {
            if (o.verificarCondicao(p)) validas.add(o);
        }
        return validas;
    }

    public boolean isFinal() { return isFinal; }
    public String getTextoNarrativo() { return "\n=== " + titulo + " ===\n" + textoNarrativo; }
    public int getId() { return id; }
}