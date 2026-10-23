package tests;

import controllers.SalvamentoController;
import models.NPC;
import models.ProgressoJogo;
import models.Protagonista;
import models.SalvamentoException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Testes de unidade de {@link SalvamentoController} e {@link SalvamentoException}.
 * Todos os arquivos são gravados em uma pasta temporária, sem tocar nos saves reais do jogo.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class SalvamentoControllerTest extends Teste {

    // Pasta temporária usada por todos os testes desta classe
    private Path pasta;

    @Override
    public void executar() throws Exception {
        Path temp = Files.createTempDirectory("saves_teste");
        this.pasta = temp;
        try {
            SalvamentoController ctrl = new SalvamentoController(temp.toString());
            testarSlots(ctrl);
            testarDadosGlobais(ctrl);
            testarSlotMaisRecente(ctrl);
            testarExcecao();
        } finally {
            apagarPasta(temp.toFile());
        }
    }

    private void testarSlots(SalvamentoController ctrl) throws Exception {
        verificar("Slot começa inexistente", !ctrl.existeSlot(1));
        verificarIgual("Slot vazio é descrito como VAZIO", "[Slot 1] --- VAZIO ---", ctrl.obterResumoVisualSlot(1));

        Protagonista p = new Protagonista("Ana \"A\"");
        p.setLogica(4); p.setCarisma(3); p.setEstresse(2); p.setCoragem(5); p.setPistas(3); p.setReputacao(7);
        p.atualizarRelacionamento(NPC.obter("Prof. Silva"), 2);
        p.atualizarRelacionamento("Beto", -1);

        ctrl.salvarSlot(2, p, 12, "O Caos no Laboratório", "Resumo com acento é ç");
        verificar("Slot passa a existir após salvar", ctrl.existeSlot(2));

        ProgressoJogo r = ctrl.carregarSlot(2);
        Protagonista c = r.getProtagonista();
        verificarIgual("Nome (com aspas) preservado", "Ana \"A\"", c.getNome());
        verificar("Todos os atributos preservados", c.getLogica() == 4 && c.getCarisma() == 3 && c.getEstresse() == 2
                && c.getCoragem() == 5 && c.getPistas() == 3 && c.getReputacao() == 7);
        verificarIgual("Relacionamentos preservados", p.getRelacionamentos(), c.getRelacionamentos());
        verificarIgual("Cena atual preservada", 12, r.getIdCenaAtual());
        verificarIgual("Título do capítulo preservado", "O Caos no Laboratório", r.getTituloCena());
        verificarIgual("Resumo preservado (UTF-8)", "Resumo com acento é ç", r.getResumoProgressao());
        verificarIgual("Número do slot preservado", 2, r.getSlotId());
        verificar("Data e hora registradas", r.getDataHora().matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"));

        String linha = ctrl.obterResumoVisualSlot(2);
        verificar("Metadados exibem nome, capítulo, data e resumo (sem número de cena)",
                linha.contains("Ana \"A\"") && linha.contains("Cap.: O Caos no Laboratório")
                        && linha.contains(r.getDataHora()) && linha.contains("Resumo com acento")
                        && !linha.contains("Cena 12"));

        ctrl.salvarSlot(2, p, 13, "Outro Capítulo", "Novo resumo");
        verificarIgual("Salvar de novo sobrescreve o slot", 13, ctrl.carregarSlot(2).getIdCenaAtual());

        ctrl.excluirSlot(2);
        verificar("Slot some após excluir", !ctrl.existeSlot(2));
        ctrl.excluirSlot(2);
        verificar("Excluir slot já vazio não causa erro", true);

        verificarLanca("Carregar slot vazio lança SalvamentoException", SalvamentoException.class, () -> ctrl.carregarSlot(3));
        verificarLanca("Slot 0 é inválido", SalvamentoException.class, () -> ctrl.salvarSlot(0, p, 1, "t", "r"));
        verificarLanca("Slot 4 é inválido", SalvamentoException.class, () -> ctrl.carregarSlot(4));
        verificarLanca("Excluir slot inválido lança exceção", SalvamentoException.class, () -> ctrl.excluirSlot(9));
        verificarLanca("Salvar protagonista nulo lança exceção", SalvamentoException.class, () -> ctrl.salvarSlot(1, null, 1, "t", "r"));

        Files.write(pasta.resolve("save_slot_3.json"), "{ lixo }".getBytes("UTF-8"));
        ProgressoJogo corrompido = ctrl.carregarSlot(3);
        verificarIgual("Save corrompido carrega com valores padrão (nome)", "Investigador", corrompido.getProtagonista().getNome());
        verificarIgual("Save corrompido carrega com valores padrão (cena)", 1, corrompido.getIdCenaAtual());
        ctrl.excluirSlot(3);
    }

    private void testarDadosGlobais(SalvamentoController ctrl) throws Exception {
        ProgressoJogo padrao = ctrl.carregarDadosGlobais();
        verificar("Sem arquivo, usa preferências padrão", padrao.getVelocidadeTextoMs() == 20
                && padrao.isAutoSaveAtivo() && padrao.isExibirDicas()
                && "pt_BR".equals(padrao.getIdiomaPref()));

        ProgressoJogo g = new ProgressoJogo();
        g.setVelocidadeTextoMs(0);
        g.setAutoSaveAtivo(false);
        g.setExibirDicas(false);
        g.setIdiomaPref("en_US");
        verificar("Primeiro desbloqueável é inédito", g.adicionarDesbloqueavel("Conquista \"1\""));
        verificar("Desbloqueável repetido é rejeitado", !g.adicionarDesbloqueavel("Conquista \"1\""));
        verificar("Primeiro final é inédito", g.adicionarFinal("Final: A"));
        verificar("Final repetido é rejeitado", !g.adicionarFinal("Final: A"));
        ctrl.salvarDadosGlobais(g);

        ProgressoJogo l = ctrl.carregarDadosGlobais();
        verificarIgual("Velocidade persistida", 0, l.getVelocidadeTextoMs());
        verificar("Auto-save desligado persistido", !l.isAutoSaveAtivo());
        verificar("Dicas desligadas persistidas", !l.isExibirDicas());
        verificarIgual("Idioma preferido persistido", "en_US", l.getIdiomaPref());
        verificar("Desbloqueáveis persistidos", l.getDesbloqueaveis().contains("Conquista \"1\""));
        verificar("Finais persistidos", l.getFinais().contains("Final: A"));
    }

    private void testarSlotMaisRecente(SalvamentoController ctrl) throws Exception {
        verificarIgual("Sem saves, não há slot recente", 0, ctrl.obterSlotMaisRecente());
        Protagonista p = new Protagonista("R");
        ctrl.salvarSlot(1, p, 1, "t", "r");
        Thread.sleep(30);
        ctrl.salvarSlot(3, p, 1, "t", "r");
        verificarIgual("Slot mais recente é o último gravado", 3, ctrl.obterSlotMaisRecente());
        Thread.sleep(30);
        ctrl.salvarSlot(1, p, 1, "t", "r");
        verificarIgual("Regravar um slot o torna o mais recente", 1, ctrl.obterSlotMaisRecente());
        ctrl.excluirSlot(1);
        ctrl.excluirSlot(3);
    }

    private void testarExcecao() {
        SalvamentoException a = new SalvamentoException("falhou");
        verificarIgual("Mensagem da exceção", "falhou", a.getMessage());
        Exception causa = new java.io.IOException("disco");
        SalvamentoException b = new SalvamentoException("falhou", causa);
        verificar("Exceção guarda a causa", b.getCause() == causa);
        verificar("SalvamentoException é checked (Exception, não RuntimeException)",
                Exception.class.isAssignableFrom(SalvamentoException.class)
                        && !RuntimeException.class.isAssignableFrom(SalvamentoException.class));
    }

    private void apagarPasta(File dir) {
        File[] itens = dir.listFiles();
        if (itens != null) for (File f : itens) f.delete();
        dir.delete();
    }
}
