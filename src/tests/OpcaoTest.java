package tests;

import models.NPC;
import models.Opcao;
import models.Protagonista;

/**
 * Testes de unidade da classe {@link Opcao}: requisitos de acesso e aplicação de consequências.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class OpcaoTest extends Teste {

    @Override
    public void executar() {
        // Opção sem requisito está sempre liberada
        Opcao livre = new Opcao("Qualquer", 2);
        verificar("Opção sem requisito é sempre liberada", livre.verificarCondicao(new Protagonista("x")));
        verificar("Protagonista nulo não bloqueia", livre.verificarCondicao(null));
        verificarIgual("Texto da opção", "Qualquer", livre.getTexto());
        verificarIgual("ID da próxima cena", 2, livre.getIdProximaCena());

        Protagonista p = new Protagonista("x");
        Opcao o = new Opcao("req", 1);

        o.setRequisito("Pistas", 1);
        verificar("Pistas: bloqueia com 0 pistas", !o.verificarCondicao(p));
        p.setPistas(1);
        verificar("Pistas: libera com 1 pista", o.verificarCondicao(p));

        o.setRequisito("CoragemAlta", 3);
        p.setCoragem(2);
        verificar("CoragemAlta: bloqueia com coragem 2", !o.verificarCondicao(p));
        p.setCoragem(3);
        verificar("CoragemAlta: libera com coragem 3", o.verificarCondicao(p));

        o.setRequisito("EstresseBaixo", 6);
        p.setEstresse(5);
        verificar("EstresseBaixo: libera com estresse 5", o.verificarCondicao(p));
        p.setEstresse(6);
        verificar("EstresseBaixo: bloqueia com estresse 6", !o.verificarCondicao(p));

        o.setRequisito("ReputacaoAlta", 1);
        p.setReputacao(0);
        verificar("ReputacaoAlta: bloqueia com reputação 0", !o.verificarCondicao(p));
        p.setReputacao(1);
        verificar("ReputacaoAlta: libera com reputação 1", o.verificarCondicao(p));

        o.setRequisito("ReputacaoBaixa", 1);
        verificar("ReputacaoBaixa: bloqueia com reputação 1", !o.verificarCondicao(p));
        p.setReputacao(0);
        verificar("ReputacaoBaixa: libera com reputação 0", o.verificarCondicao(p));

        o.setRequisito("FlagranteFalha", 0);
        Protagonista medroso = new Protagonista("m");
        medroso.setEstresse(5);
        medroso.setCoragem(4);
        verificar("FlagranteFalha: libera com estresse alto e coragem baixa", o.verificarCondicao(medroso));
        Protagonista corajoso = new Protagonista("c");
        corajoso.setEstresse(7);
        corajoso.setCoragem(10);
        verificar("FlagranteFalha: bloqueia com coragem alta", !o.verificarCondicao(corajoso));
        Protagonista calmo = new Protagonista("k");
        calmo.setEstresse(2);
        calmo.setCoragem(3);
        verificar("FlagranteFalha: bloqueia com estresse baixo", !o.verificarCondicao(calmo));

        o.setRequisito("Desconhecido", 99);
        verificar("Requisito desconhecido não bloqueia", o.verificarCondicao(new Protagonista("z")));
        o.setRequisito(null, 0);
        verificar("Requisito nulo equivale a sem requisito", o.verificarCondicao(new Protagonista("z")));

        // Consequências
        Protagonista q = new Protagonista("q");
        q.setEstresse(2);
        Opcao c = new Opcao("efeitos", 3);
        NPC beto = NPC.obter("Beto");
        c.setConsequencias(1, 2, -1, 3, 1, 4, beto, 2);
        String resumo = c.aplicarConsequencias(q);
        verificarIgual("Lógica aplicada", 1, q.getLogica());
        verificarIgual("Carisma aplicado", 2, q.getCarisma());
        verificarIgual("Estresse reduzido", 1, q.getEstresse());
        verificarIgual("Coragem aplicada", 3, q.getCoragem());
        verificarIgual("Pistas aplicadas", 1, q.getPistas());
        verificarIgual("Reputação aplicada", 4, q.getReputacao());
        verificarIgual("Relacionamento com o NPC aplicado", 2, q.getRelacionamento(beto));
        verificar("Resumo cita atributos alterados", resumo.contains("[Lógica: +1]") && resumo.contains("[Estresse: -1]")
                && resumo.contains("Relação com Beto"));
        verificarIgual("NPC da opção é o do catálogo", beto, c.getNpc());

        Opcao vazia = new Opcao("sem efeito", 4);
        vazia.setConsequencias(0, 0, 0, 0, 0, 0, null, 0);
        verificarIgual("Opção sem efeitos devolve resumo vazio", "", vazia.aplicarConsequencias(new Protagonista("v")));
        verificarIgual("Protagonista nulo devolve resumo vazio", "", c.aplicarConsequencias(null));

        Protagonista piso = new Protagonista("piso");
        Opcao negativa = new Opcao("pesada", 5);
        negativa.setConsequencias(0, 0, 0, 0, 0, -10, null, 0);
        negativa.aplicarConsequencias(piso);
        verificarIgual("Consequência negativa respeita o piso zero", 0, piso.getReputacao());
    }
}
