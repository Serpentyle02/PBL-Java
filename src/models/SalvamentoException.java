package models;

/**
 * Exceção verificada (checked exception) customizada para tratar falhas
 * de leitura, gravação ou validação de slots durante a persistência do jogo.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class SalvamentoException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Construtor com mensagem descritiva do erro de persistência.
     *
     * @param mensagem Descrição da falha ocorrida.
     */
    public SalvamentoException(String mensagem) {
        super(mensagem);
    }

    /**
     * Construtor com mensagem descritiva e a exceção original que causou o erro.
     *
     * @param mensagem Descrição da falha ocorrida.
     * @param causa    Exceção raiz (ex.: IOException).
     */
    public SalvamentoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
