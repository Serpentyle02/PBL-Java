package models;

/**
 * Estrutura base abstrata de uma cena no jogo.
 * Define o contrato polimórfico para carregamento de dados.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public abstract class CenaBase {
    protected int id;
    protected String titulo = "";
    protected String texto = "";
    protected boolean isFinal;

    public CenaBase() {
        this.id = -1;
    }

    public CenaBase(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    /** Interpreta os dados brutos (ex: JSON) de uma cena. */
    public abstract void carregarDados(String dados);

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getTexto() { return texto; }
    public boolean isFinal() { return isFinal; }
}