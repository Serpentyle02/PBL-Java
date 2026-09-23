import java.util.ArrayList;
import java.util.List;

/**
 * Estrutura base abstrata de uma cena no jogo.
 * Define o contrato polimórfico para carregamento de dados e gestão de opções.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public abstract class CenaBase {
    // Atributos identificadores e informativos da cena
    protected int id;
    protected String titulo;
    protected String textoNarrativo;
    protected boolean isFinal; // Indica se a cena encerra a história
    protected List<Opcao> opcoes = new ArrayList<>(); // Lista de decisões possíveis

    /**
     * Método abstrato polimórfico para interpretação de dados da cena.
     * @param dados Conteúdo textual em formato específico (ex: JSON).
     */
    public abstract void carregarDados(String dados);

    /**
     * Adiciona uma opção de escolha à cena.
     */
    public void adicionarOpcao(Opcao o) { 
        this.opcoes.add(o); // Insere o objeto Opcao na lista da cena
    }

    /**
     * Filtra as opções cujos requisitos são satisfeitos pelos atributos do jogador.
     * @param p Instância do Protagonista.
     * @return Lista contendo apenas as opções válidas.
     */
    public List<Opcao> getOpcoes(Protagonista p) {
        List<Opcao> validas = new ArrayList<>(); // Lista auxiliar de válidos
        for (Opcao o : opcoes) {
            // Avalia as condições dinâmicas de cada opção
            if (o.verificarCondicao(p)) validas.add(o);
        }
        return validas; // Retorna a lista filtrada
    }

    // Métodos seletores da cena
    public boolean isFinal() { return isFinal; }
    public String getTextoNarrativo() { return "\n=== " + titulo + " ===\n" + textoNarrativo; }
    public int getId() { return id; }
}