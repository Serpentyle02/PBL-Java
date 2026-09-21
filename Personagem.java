/**
 * Classe abstrata base para representação de personagens no sistema.
 * Aplica os conceitos de Abstração e Encapsulamento da POO.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public abstract class Personagem {
    // Nome do personagem (acessível por subclasses através do modificador protected)
    protected String nome;
    
    /**
     * Construtor da classe Personagem.
     * @param nome Nome do personagem.
     */
    public Personagem(String nome) {
        this.nome = nome; // Inicializa o nome da entidade
    }
    
    /**
     * Obtém o nome do personagem.
     * @return String com o nome.
     */
    public String getNome() { 
        return nome; // Retorna o identificador do personagem
    }
}