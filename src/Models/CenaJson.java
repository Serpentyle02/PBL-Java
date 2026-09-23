import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementação concreta de CenaBase responsável pelo parser nativo de JSON.
 * Isola dependências externas extraindo dados via expressões regulares.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class CenaJson extends CenaBase {
    
    /**
     * Construtor que recebe a String JSON do bloco e executa o carregamento.
     */
    public CenaJson(String jsonString) {
        carregarDados(jsonString); // Invoca a extração de dados
    }

    @Override
    public void carregarDados(String json) {
        // Extrai dados primários da cena utilizando expressões regulares
        this.id = Integer.parseInt(extrairValor(json, "\"id\"\\s*:\\s*(\\d+)"));
        this.titulo = sanitizarTexto(extrairValor(json, "\"titulo\"\\s*:\\s*\"([^\"]+)\""));
        this.textoNarrativo = sanitizarTexto(extrairValor(json, "\"texto\"\\s*:\\s*\"([^\"]+)\""));
        this.isFinal = Boolean.parseBoolean(extrairValor(json, "\"isFinal\"\\s*:\\s*(true|false)"));

        // Localiza e varre os sub-blocos de opções contidos no JSON da cena
        Matcher m = Pattern.compile("\\{\\s*\"texto\"\\s*:\\s*\"([^\"]+)\"(.*?)\\}", Pattern.DOTALL).matcher(json);
        
        while (m.find()) {
            String texto = sanitizarTexto(m.group(1)); // Extrai o texto visível da opção
            String opJson = m.group(2); // Extrai os modificadores e requisitos numéricos
            
            int idProx = Integer.parseInt(extrairValor(opJson, "\"idProximaCena\"\\s*:\\s*(\\d+)"));
            Opcao op = new Opcao(texto, idProx); // Instancia a opção
            
            // Lê modificadores de atributos com fallback seguro para zero
            int mLog = parseIntSeguro(extrairValor(opJson, "\"modLog\"\\s*:\\s*(-?\\d+)"));
            int mCar = parseIntSeguro(extrairValor(opJson, "\"modCar\"\\s*:\\s*(-?\\d+)"));
            int mEst = parseIntSeguro(extrairValor(opJson, "\"modEst\"\\s*:\\s*(-?\\d+)"));
            int mCor = parseIntSeguro(extrairValor(opJson, "\"modCor\"\\s*:\\s*(-?\\d+)"));
            int mPis = parseIntSeguro(extrairValor(opJson, "\"modPis\"\\s*:\\s*(-?\\d+)"));
            int mRep = parseIntSeguro(extrairValor(opJson, "\"modRep\"\\s*:\\s*(-?\\d+)"));
            
            // Processa alvos de relacionamento (NPCs)
            String npcNome = extrairValor(opJson, "\"npc\"\\s*:\\s*\"([^\"]*)\"");
            int mRel = parseIntSeguro(extrairValor(opJson, "\"modRel\"\\s*:\\s*(-?\\d+)"));
            NPC npc = npcNome.isEmpty() ? null : new NPC(npcNome, "");
            
            // Associa os modificadores à opção
            op.setConsequencias(mLog, mCar, mEst, mCor, mPis, mRep, npc, mRel);

            // Lê requisitos condicionais de acesso, caso existam
            String reqAttr = extrairValor(opJson, "\"reqAttr\"\\s*:\\s*\"([^\"]*)\"");
            int reqVal = parseIntSeguro(extrairValor(opJson, "\"reqVal\"\\s*:\\s*(\\d+)"));
            if (!reqAttr.isEmpty()) {
                op.setRequisito(reqAttr, reqVal); // Configura exigência para exibição
            }
            
            this.adicionarOpcao(op); // Adiciona a opção à lista da cena
        }
    }

    /**
     * Sanitiza a String substituindo escapes literais de quebra de linha por caracteres reais.
     */
    private String sanitizarTexto(String texto) {
        if (texto == null) return ""; // Previne NullPointerException
        return texto.replace("\\\\n", "\n").replace("\\n", "\n"); // Converte \n e \\n
    }

    /**
     * Executa a busca de padrões regex retornando o grupo capturado.
     */
    private String extrairValor(String texto, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(texto);
        return matcher.find() ? matcher.group(1) : ""; // Retorna valor encontrado ou String vazia
    }

    /**
     * Converte Strings numéricas com segurança contra valores nulos ou vazios.
     */
    private int parseIntSeguro(String val) {
        return (val == null || val.isEmpty()) ? 0 : Integer.parseInt(val); // Garante retorno numérico
    }
}