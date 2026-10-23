package tests;

import models.CenaJson;
import models.Conquista;
import models.Conquistas;
import models.Opcao;
import models.ProgressoJogo;
import models.Protagonista;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Testes de unidade do catálogo {@link Conquistas}: títulos únicos, desbloqueio sem repetição,
 * meta-conquistas e, principalmente, a garantia de que TODA conquista é alcançável pelo roteiro real
 * (busca exaustiva de caminhos, atributos e relacionamentos).
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class ConquistasTest extends Teste {

    private final Set<String> visitados = new HashSet<>();
    private final Set<String> alcancaveis = new HashSet<>();
    private Map<Integer, CenaJson> porId;
    // Estado global neutro (sem finais nem conquistas), para avaliar só as regras de atributo/cena
    private final ProgressoJogo neutro = new ProgressoJogo();

    @Override
    public void executar() {
        testarCatalogo();
        testarDesbloqueio();
        testarMetaConquistas();
        testarAlcanceNoRoteiro();
    }

    private void testarCatalogo() {
        List<Conquista> todas = Conquistas.todas();
        verificar("Catálogo tem pelo menos 15 conquistas (" + todas.size() + ")", todas.size() >= 15);
        Set<String> titulos = new HashSet<>();
        boolean descricoes = true;
        for (Conquista c : todas) {
            titulos.add(c.getTitulo());
            if (c.getDescricao() == null || c.getDescricao().trim().isEmpty()) descricoes = false;
        }
        verificarIgual("Títulos são únicos", todas.size(), titulos.size());
        verificar("Toda conquista tem descrição", descricoes);
        verificar("As conquistas originais continuam no catálogo",
                titulos.contains("Conquista: Primeiros Passos na Investigação")
                        && titulos.contains("Dossiê Confidencial: A Testemunha Oculta"));

        boolean traduzidas = true;
        for (Conquista c : todas) {
            if (c.getTitulo("en_US").equals(c.getTitulo("pt_BR"))
                    || c.getDescricao("en_US").equals(c.getDescricao("pt_BR"))
                    || c.getDescricao("es_ES").equals(c.getDescricao("pt_BR"))) {
                traduzidas = false;
            }
        }
        verificar("Todas as conquistas possuem tradução para en_US e es_ES", traduzidas);
    }

    private void testarDesbloqueio() {
        ProgressoJogo g = new ProgressoJogo();
        Protagonista p = new Protagonista("x");

        List<Conquista> novas = Conquistas.verificar(p, 1, false, g);
        verificarIgual("Estado inicial não desbloqueia nada", 0, novas.size());

        novas = Conquistas.verificar(p, 2, false, g);
        verificarIgual("Cena 2 desbloqueia Primeiros Passos", 1, novas.size());
        verificar("Conquista registrada nos dados globais",
                g.getDesbloqueaveis().contains("Conquista: Primeiros Passos na Investigação"));
        verificarIgual("A mesma conquista não é concedida duas vezes", 0, Conquistas.verificar(p, 2, false, g).size());

        p.setLogica(10);
        p.setPistas(5);
        List<Conquista> varias = Conquistas.verificar(p, 3, false, g);
        verificarIgual("Atributos altos desbloqueiam várias conquistas de uma vez", 2, varias.size());

        Protagonista frio = new Protagonista("f");
        ProgressoJogo g2 = new ProgressoJogo();
        List<Conquista> fim = Conquistas.verificar(frio, 201, true, g2);
        boolean mente = false, detetive = false;
        for (Conquista c : fim) {
            if (c.getTitulo().equals("Conquista: Mente Fria")) mente = true;
            if (c.getTitulo().equals("Conquista: Detetive Acadêmico")) detetive = true;
        }
        verificar("Final do Detetive com estresse 0 libera Detetive Acadêmico e Mente Fria", mente && detetive);

        ProgressoJogo g3 = new ProgressoJogo();
        Protagonista estressado = new Protagonista("e");
        estressado.setEstresse(5);
        List<Conquista> semFrio = Conquistas.verificar(estressado, 22, true, g3);
        boolean temMenteFria = false;
        for (Conquista c : semFrio) if (c.getTitulo().equals("Conquista: Mente Fria")) temMenteFria = true;
        verificar("Mente Fria exige estresse zerado", !temMenteFria);
    }

    private void testarMetaConquistas() {
        // Veterano: só com os 4 finais registrados
        ProgressoJogo g = new ProgressoJogo();
        g.adicionarFinal("A"); g.adicionarFinal("B"); g.adicionarFinal("C");
        verificar("Veterano não sai com 3 finais", !contem(Conquistas.verificar(new Protagonista("x"), 1, false, g), Conquistas.VETERANO));
        g.adicionarFinal("D");
        verificar("Veterano sai com 4 finais", contem(Conquistas.verificar(new Protagonista("x"), 1, false, g), Conquistas.VETERANO));

        // Mestre: só com todas as outras desbloqueadas
        ProgressoJogo m = new ProgressoJogo();
        for (Conquista c : Conquistas.todas()) {
            if (!c.getTitulo().equals(Conquistas.MESTRE)) m.adicionarDesbloqueavel(c.getTitulo());
        }
        // Remove uma para provar que a meta depende de todas
        m.getDesbloqueaveis().remove("Conquista: Sem Medo");
        verificar("Mestre não sai faltando uma conquista", !contem(Conquistas.verificar(new Protagonista("x"), 1, false, m), Conquistas.MESTRE));
        m.adicionarDesbloqueavel("Conquista: Sem Medo");
        verificar("Mestre sai com todas as outras", contem(Conquistas.verificar(new Protagonista("x"), 1, false, m), Conquistas.MESTRE));
    }

    private boolean contem(List<Conquista> lista, String titulo) {
        for (Conquista c : lista) if (c.getTitulo().equals(titulo)) return true;
        return false;
    }

    private void testarAlcanceNoRoteiro() {
        List<CenaJson> cenas = CenaJson.carregarCenasDoArquivo();
        porId = new HashMap<>();
        for (CenaJson c : cenas) porId.put(c.getId(), c);
        if (cenas.isEmpty()) {
            verificar("Roteiro carregado para testar alcance das conquistas", false);
            return;
        }
        explorar(cenas.get(0).getId(), new Protagonista("teste"));

        List<String> inalcancaveis = new ArrayList<>();
        for (Conquista c : Conquistas.todas()) {
            if (c.getTitulo().equals(Conquistas.VETERANO) || c.getTitulo().equals(Conquistas.MESTRE)) continue;
            if (!alcancaveis.contains(c.getTitulo())) inalcancaveis.add(c.getTitulo());
        }
        verificar("Toda conquista de atributo/cena/final é alcançável no roteiro real " + inalcancaveis,
                inalcancaveis.isEmpty());
    }

    private static String chave(int id, Protagonista p) {
        return id + "|" + p.getLogica() + "," + p.getCarisma() + "," + p.getEstresse() + "," + p.getCoragem()
                + "," + p.getPistas() + "," + p.getReputacao() + "|" + p.getRelacionamentos();
    }

    private static Protagonista copiar(Protagonista p) {
        Protagonista q = new Protagonista(p.getNome());
        q.setLogica(p.getLogica());
        q.setCarisma(p.getCarisma());
        q.setEstresse(p.getEstresse());
        q.setCoragem(p.getCoragem());
        q.setPistas(p.getPistas());
        q.setReputacao(p.getReputacao());
        for (Map.Entry<String, Integer> e : p.getRelacionamentos().entrySet()) {
            q.atualizarRelacionamento(e.getKey(), e.getValue());
        }
        return q;
    }

    private void explorar(int id, Protagonista p) {
        if (!visitados.add(chave(id, p))) return;
        CenaJson cena = porId.get(id);
        boolean fim = cena.isFinal() || cena.getOpcoes().isEmpty();
        for (Conquista c : Conquistas.todas()) {
            if (c.foiAtingida(p, id, fim, neutro)) alcancaveis.add(c.getTitulo());
        }
        if (fim) return;
        for (Opcao o : cena.getOpcoes()) {
            if (!o.verificarCondicao(p)) continue;
            Protagonista q = copiar(p);
            o.aplicarConsequencias(q);
            explorar(o.getIdProximaCena(), q);
        }
    }
}
