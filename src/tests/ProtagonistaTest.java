package tests;

import models.NPC;
import models.Protagonista;

/**
 * Testes de unidade das classes {@link Protagonista} e {@link NPC}:
 * trava numérica dos atributos, relacionamentos e catálogo de NPCs.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class ProtagonistaTest extends Teste {

    @Override
    public void executar() {
        Protagonista p = new Protagonista("Ana");
        verificarIgual("Nome é guardado pela superclasse Personagem", "Ana", p.getNome());
        verificarIgual("Atributos começam em zero", 0, p.getLogica() + p.getCarisma() + p.getEstresse()
                + p.getCoragem() + p.getPistas() + p.getReputacao());

        p.setLogica(3);
        verificarIgual("setLogica altera o valor", 3, p.getLogica());
        p.setLogica(-5);
        verificarIgual("Lógica nunca fica negativa (trava em 0)", 0, p.getLogica());
        p.setEstresse(-1);
        verificarIgual("Estresse nunca fica negativo", 0, p.getEstresse());
        p.setCoragem(-2);
        verificarIgual("Coragem nunca fica negativa", 0, p.getCoragem());
        p.setCarisma(-2);
        verificarIgual("Carisma nunca fica negativo", 0, p.getCarisma());
        p.setPistas(-2);
        verificarIgual("Pistas nunca ficam negativas", 0, p.getPistas());
        p.setReputacao(-10);
        verificarIgual("Reputação nunca fica negativa", 0, p.getReputacao());

        // Relacionamentos por nome e por objeto NPC
        p.atualizarRelacionamento("Beto", 2);
        p.atualizarRelacionamento("Beto", -1);
        verificarIgual("Relacionamento por nome acumula deltas", 1, p.getRelacionamentos().get("Beto"));
        p.atualizarRelacionamento("   ", 5);
        p.atualizarRelacionamento((String) null, 5);
        p.atualizarRelacionamento("Luiza", 0);
        verificarIgual("Nome vazio, nulo ou delta zero são ignorados", 1, p.getRelacionamentos().size());

        NPC silva = NPC.obter("Prof. Silva");
        p.atualizarRelacionamento(silva, 3);
        verificarIgual("Relacionamento por objeto NPC usa o nome do NPC", 3, p.getRelacionamento(silva));
        verificarIgual("NPC sem interação tem afinidade 0", 0, p.getRelacionamento(NPC.obter("Luiza")));
        p.atualizarRelacionamento((NPC) null, 9);
        verificarIgual("NPC nulo é ignorado", 2, p.getRelacionamentos().size());

        // Catálogo de NPCs
        verificar("NPC.obter devolve sempre a mesma instância para o mesmo nome", NPC.obter("Beto") == NPC.obter("Beto"));
        verificarIgual("Beto é o Rival", "Rival", NPC.obter("Beto").getPapelNaHistoria());
        verificarIgual("Luiza é a Hacker", "Hacker", NPC.obter("Luiza").getPapelNaHistoria());
        verificarIgual("Prof. Silva é o Orientador", "Orientador", NPC.obter("Prof. Silva").getPapelNaHistoria());
        verificarIgual("NPC desconhecido recebe papel genérico", "Personagem secundário", NPC.obter("Fulano").getPapelNaHistoria());
        verificar("Nome vazio resulta em NPC nulo", NPC.obter("") == null && NPC.obter(null) == null);
        verificarIgual("NPC herda o nome de Personagem", "Beto", NPC.obter("Beto").getNome());
    }
}
