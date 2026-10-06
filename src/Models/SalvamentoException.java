package models;

/**
 * Exceção customizada para tratar falhas durante o salvamento do jogo.
 */
public class SalvamentoException extends Exception {
    
    public SalvamentoException(String mensagem) {
        super(mensagem);
    }
    
    public SalvamentoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}