package tests;

/**
 * Executor dos testes de unidade (Java puro, sem JUnit).
 * Roda todas as classes de teste, imprime o resultado de cada verificação e
 * encerra com código de saída diferente de zero se algum teste falhar.
 *
 * Uso: {@code java -cp bin tests.TestRunner} (a partir da pasta raiz do projeto).
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class TestRunner {

    public static void main(String[] args) {
        Teste[] suites = {
            new ProtagonistaTest(),
            new OpcaoTest(),
            new CenaJsonTest(),
            new ConquistasTest(),
            new SalvamentoControllerTest()
        };

        for (Teste suite : suites) {
            System.out.println("\n== " + suite.getClass().getSimpleName() + " ==");
            try {
                suite.executar();
            } catch (Exception e) {
                Teste.total++;
                Teste.falhas++;
                System.out.println("  [FALHA] exceção inesperada em " + suite.getClass().getSimpleName() + ": " + e);
                e.printStackTrace(System.out);
            }
        }

        System.out.println("\n========================================");
        System.out.println(" Verificações: " + Teste.total + " | Falhas: " + Teste.falhas);
        System.out.println(Teste.falhas == 0 ? " TODOS OS TESTES PASSARAM" : " HÁ TESTES FALHANDO");
        System.out.println("========================================");
        System.exit(Teste.falhas == 0 ? 0 : 1);
    }
}
