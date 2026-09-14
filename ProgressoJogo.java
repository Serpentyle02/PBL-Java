public class ProgressoJogo {
    private int idCenaAtual;
    private Protagonista jogador;

    public ProgressoJogo(Protagonista p) {
        this.jogador = p;
        this.idCenaAtual = 0; 
    }

    public int getCenaAtual() { return idCenaAtual; }
    public void setCenaAtual(int id) { this.idCenaAtual = id; }
    public Protagonista getJogador() { return jogador; }
}