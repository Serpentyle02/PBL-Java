package tests;

/**
 * Mini framework de testes de unidade em Java puro (sem JUnit), mantendo o projeto sem dependências externas.
 * Cada classe de teste estende esta base e usa os métodos {@code verificar*} para registrar resultados.
 * Os contadores são globais e consolidados pelo {@link TestRunner}.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public abstract class Teste {

    // Contadores globais de verificações executadas e falhas encontradas
    static int total = 0;
    static int falhas = 0;

    // Nome da classe de teste em execução (usado nas mensagens)
    private final String grupo = getClass().getSimpleName();

    /**
     * Executa todos os casos de teste da classe.
     */
    public abstract void executar() throws Exception;

    /**
     * Registra uma verificação booleana.
     *
     * @param descricao Descrição do comportamento esperado.
     * @param condicao  Resultado da verificação (deve ser verdadeiro para passar).
     */
    protected void verificar(String descricao, boolean condicao) {
        total++;
        if (condicao) {
            System.out.println("  [OK]    " + grupo + ": " + descricao);
        } else {
            falhas++;
            System.out.println("  [FALHA] " + grupo + ": " + descricao);
        }
    }

    /**
     * Registra uma verificação de igualdade entre o valor esperado e o obtido.
     */
    protected void verificarIgual(String descricao, Object esperado, Object obtido) {
        boolean ok = (esperado == null) ? obtido == null : esperado.equals(obtido);
        verificar(descricao + (ok ? "" : " (esperado=" + esperado + ", obtido=" + obtido + ")"), ok);
    }

    /**
     * Verifica que a execução do trecho informado lança uma exceção do tipo esperado.
     */
    protected void verificarLanca(String descricao, Class<? extends Throwable> tipo, Executavel trecho) {
        try {
            trecho.executar();
            verificar(descricao + " (nenhuma exceção lançada)", false);
        } catch (Throwable t) {
            verificar(descricao, tipo.isInstance(t));
        }
    }

    /**
     * Trecho de código que pode lançar exceções (usado em {@link #verificarLanca}).
     */
    protected interface Executavel {
        void executar() throws Exception;
    }
}
