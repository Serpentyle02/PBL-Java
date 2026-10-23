package tests;

import models.CenaBase;
import models.CenaJson;
import models.IdiomaManager;
import models.Opcao;
import models.Protagonista;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Testes de unidade de {@link CenaJson} e {@link IdiomaManager}: interpretação de blocos JSON,
 * internacionalização e validação de integridade do roteiro real nos idiomas suportados
 * (destinos, becos sem saída e alcance dos 4 finais).
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class CenaJsonTest extends Teste {

    // Estados (cena + atributos) já visitados na busca exaustiva
    private final Set<String> visitados = new HashSet<>();
    private final Set<Integer> finaisAlcancados = new TreeSet<>();
    private int becosSemSaida = 0;
    private Map<Integer, CenaJson> porId;

    @Override
    public void executar() {
        testarParserDeBloco();
        testarRoteiroReal();
        testarIdiomasERoteirosTraduzidos();
    }

    private void testarParserDeBloco() {
        String bloco = "{ \"id\": 7, \"titulo\": \"T\\u00edtulo X\", \"texto\": \"Linha 1\\nLinha \\\"2\\\"\", \"isFinal\": false,"
                + " \"opcoes\": [ { \"texto\": \"Op A\", \"idProximaCena\": 8, \"modLog\": 2, \"modCar\": 0, \"modEst\": -1,"
                + " \"modCor\": 0, \"modPis\": 1, \"modRep\": 0, \"npc\": \"Beto\", \"modRel\": 2,"
                + " \"reqAttr\": \"Pistas\", \"reqVal\": 1 },"
                + " { \"texto\": \"Op B\", \"idProximaCena\": 9, \"modLog\": 0, \"modCar\": 0, \"modEst\": 0,"
                + " \"modCor\": 0, \"modPis\": 0, \"modRep\": 0, \"npc\": \"\", \"modRel\": 0, \"reqAttr\": \"\", \"reqVal\": 0 } ] }";
        CenaJson cena = new CenaJson();
        cena.carregarDados(bloco);

        verificarIgual("Parser lê o id", 7, cena.getId());
        verificarIgual("Parser lê o título (com escape unicode)", "Título X", cena.getTitulo());
        verificarIgual("Parser lê o texto (quebra de linha e aspas escapadas)", "Linha 1\nLinha \"2\"", cena.getTexto());
        verificar("Cena com opções não é final", !cena.isFinal());
        verificarIgual("Parser lê as duas opções", 2, cena.getOpcoes().size());

        Opcao a = cena.getOpcoes().get(0);
        verificarIgual("Opção A: texto", "Op A", a.getTexto());
        verificarIgual("Opção A: destino", 8, a.getIdProximaCena());
        verificarIgual("Opção A: NPC convertido em objeto", "Beto", a.getNpc().getNome());
        verificar("Opção A: requisito de pistas bloqueia protagonista sem pistas", !a.verificarCondicao(new Protagonista("x")));
        Protagonista p = new Protagonista("x");
        a.aplicarConsequencias(p);
        verificarIgual("Opção A: efeito de lógica", 2, p.getLogica());
        verificarIgual("Opção A: efeito de pistas", 1, p.getPistas());
        verificarIgual("Opção A: relação com Beto", 2, p.getRelacionamentos().get("Beto"));
        verificar("Opção B: sem NPC", cena.getOpcoes().get(1).getNpc() == null);

        CenaJson fim = new CenaJson();
        fim.carregarDados("{ \"id\": 99, \"titulo\": \"FIM\", \"texto\": \"acabou\", \"isFinal\": true, \"opcoes\": [] }");
        verificar("Parser reconhece cena final (isFinal)", fim.isFinal());
        verificarIgual("Cena final não tem opções", 0, fim.getOpcoes().size());

        CenaJson vazia = new CenaJson();
        vazia.carregarDados("{ \"titulo\": \"sem id\" }");
        verificarIgual("Bloco sem id resulta em id -1", -1, vazia.getId());
        verificarIgual("Cena nova começa com id inválido", -1, new CenaJson().getId());
        verificar("CenaJson é uma CenaBase (polimorfismo)", new CenaJson() instanceof CenaBase);
    }

    private void testarRoteiroReal() {
        List<CenaJson> cenas = CenaJson.carregarCenasDoArquivo();
        verificarIgual("Roteiro real carrega 24 cenas", 24, cenas.size());
        if (cenas.isEmpty()) return;

        porId = new HashMap<>();
        boolean ordenado = true;
        int anterior = Integer.MIN_VALUE;
        for (CenaJson c : cenas) {
            porId.put(c.getId(), c);
            if (c.getId() < anterior) ordenado = false;
            anterior = c.getId();
        }
        verificar("Cenas vêm ordenadas por id", ordenado);

        int destinosInvalidos = 0;
        int semOpcaoNaoFinal = 0;
        for (CenaJson c : cenas) {
            if (!c.isFinal() && c.getOpcoes().isEmpty()) semOpcaoNaoFinal++;
            for (Opcao o : c.getOpcoes()) {
                if (!porId.containsKey(o.getIdProximaCena())) destinosInvalidos++;
            }
        }
        verificarIgual("Todos os destinos das opções existem", 0, destinosInvalidos);
        verificarIgual("Toda cena não final tem opções", 0, semOpcaoNaoFinal);

        int totalFinais = 0;
        for (CenaJson c : cenas) if (c.isFinal()) totalFinais++;
        verificarIgual("O roteiro tem 4 finais", 4, totalFinais);

        // Busca exaustiva de todas as combinações de escolhas e atributos a partir da primeira cena
        explorar(cenas.get(0).getId(), new Protagonista("teste"));
        verificarIgual("Nenhum beco sem saída (toda cena tem ao menos uma opção liberada)", 0, becosSemSaida);
        verificar("Os 4 finais são alcançáveis " + finaisAlcancados, finaisAlcancados.size() == 4);
    }

    private void testarIdiomasERoteirosTraduzidos() {
        IdiomaManager mgr = new IdiomaManager();
        verificarIgual("Idioma padrão inicial é pt_BR", "pt_BR", mgr.getIdiomaAtual());
        verificar("Chave de interface pt_BR carregada", !mgr.get("MENU_OP1").equals("MENU_OP1"));

        mgr.setIdioma("en_US");
        verificarIgual("Idioma muda para en_US", "en_US", mgr.getIdiomaAtual());
        verificar("Chave de interface en_US carregada", mgr.get("MENU_OP1").contains("New Game"));

        mgr.setIdioma("es_ES");
        verificarIgual("Idioma muda para es_ES", "es_ES", mgr.getIdiomaAtual());
        verificar("Chave de interface es_ES carregada", mgr.get("MENU_OP1").contains("Nueva Partida"));

        mgr.setIdioma(null);
        verificarIgual("Idioma inválido/nulo faz fallback para pt_BR", "pt_BR", mgr.getIdiomaAtual());

        verificarIgual("Roteiro en_US carrega 24 cenas", 24, CenaJson.carregarCenasDoArquivo("en_US").size());
        verificarIgual("Roteiro es_ES carrega 24 cenas", 24, CenaJson.carregarCenasDoArquivo("es_ES").size());
    }

    private static String chave(int id, Protagonista p) {
        return id + "|" + p.getLogica() + "," + p.getCarisma() + "," + p.getEstresse() + ","
                + p.getCoragem() + "," + p.getPistas() + "," + p.getReputacao();
    }

    private static Protagonista copiar(Protagonista p) {
        Protagonista q = new Protagonista(p.getNome());
        q.setLogica(p.getLogica());
        q.setCarisma(p.getCarisma());
        q.setEstresse(p.getEstresse());
        q.setCoragem(p.getCoragem());
        q.setPistas(p.getPistas());
        q.setReputacao(p.getReputacao());
        return q;
    }

    private void explorar(int id, Protagonista p) {
        if (!visitados.add(chave(id, p))) return;
        CenaJson cena = porId.get(id);
        if (cena.isFinal() || cena.getOpcoes().isEmpty()) {
            finaisAlcancados.add(id);
            return;
        }
        boolean algumaLiberada = false;
        for (Opcao o : cena.getOpcoes()) {
            if (!o.verificarCondicao(p)) continue;
            algumaLiberada = true;
            Protagonista q = copiar(p);
            o.aplicarConsequencias(q);
            explorar(o.getIdProximaCena(), q);
        }
        if (!algumaLiberada) becosSemSaida++;
    }
}
