/**
 * Guarda o estado corrente da execução do jogo no modelo.
 * Mantém a referência do Protagonista e o cursor da cena atual.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class ProgressoJogo {
    private int idCenaAtual; // ID numérico da cena em execução
    private Protagonista jogador; // Referência do jogador

    /**
     * Instancia o progresso associando o Protagonista na cena inicial (1).
     */
    public ProgressoJogo(Protagonista p) {
        this.jogador = p;
        this.idCenaAtual = 1; // Inicia a história pela primeira cena
    }

    public int getCenaAtual() { 
        return idCenaAtual; // Retorna o ID da cena corrente
    }

    public void setCenaAtual(int id) { 
        this.idCenaAtual = id; // Atualiza o cursor da cena corrente
    }

    public Protagonista getJogador() { 
        return jogador; // Retorna o objeto do protagonista
    }
}