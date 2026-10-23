package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Estrutura base abstrata de uma cena no jogo.
 * Define os atributos fundamentais de qualquer cena e o contrato polimórfico
 * ({@link #carregarDados(String)}) para interpretação de diferentes fontes de dados.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public abstract class CenaBase {

    // Identificador numérico único da cena no roteiro
    protected int id;
    // Título temático exibido no topo da cena
    protected String titulo = "";
    // Conteúdo narrativo principal e diálogos da cena
    protected String texto = "";
    // Flag que indica se esta cena representa um desfecho (fim de partida)
    protected boolean isFinal;
    // Lista de opções de decisão disponíveis nesta cena
    protected List<Opcao> opcoes = new ArrayList<>();

    /**
     * Construtor padrão. Inicializa a cena com ID inválido (-1) até que os dados sejam carregados.
     */
    public CenaBase() {
        this.id = -1;
    }

    /**
     * Construtor parametrizado para criação direta de uma cena com ID e texto.
     *
     * @param id    Identificador numérico da cena.
     * @param texto Narrativa principal da cena.
     */
    public CenaBase(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    /**
     * Método abstrato polimórfico responsável por interpretar e popular os dados brutos de uma cena.
     *
     * @param dados Bloco textual bruto (ex.: objeto JSON) com os dados da cena.
     */
    public abstract void carregarDados(String dados);

    // Métodos seletores (Getters) dos atributos base da cena
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getTexto() { return texto; }
    public boolean isFinal() { return isFinal; }
    public List<Opcao> getOpcoes() { return opcoes; }
}
