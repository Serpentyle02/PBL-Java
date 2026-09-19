import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CenaJson extends CenaBase {
    
    public CenaJson(String jsonString) {
        carregarDados(jsonString);
    }

    @Override
    public void carregarDados(String json) {
        this.id = Integer.parseInt(extrairValor(json, "\"id\"\\s*:\\s*(\\d+)"));
        
        // Trata barra dupla (\\\\n) e barra simples (\\n) para não deixar barras sobrando
        this.titulo = sanitizarTexto(extrairValor(json, "\"titulo\"\\s*:\\s*\"([^\"]+)\""));
        this.textoNarrativo = sanitizarTexto(extrairValor(json, "\"texto\"\\s*:\\s*\"([^\"]+)\""));
        this.isFinal = Boolean.parseBoolean(extrairValor(json, "\"isFinal\"\\s*:\\s*(true|false)"));

        Matcher m = Pattern.compile("\\{\\s*\"texto\"\\s*:\\s*\"([^\"]+)\"(.*?)\\}", Pattern.DOTALL).matcher(json);
        
        while (m.find()) {
            String texto = sanitizarTexto(m.group(1));       
            String opJson = m.group(2);      
            
            int idProx = Integer.parseInt(extrairValor(opJson, "\"idProximaCena\"\\s*:\\s*(\\d+)"));
            Opcao op = new Opcao(texto, idProx);
            
            int mLog = parseIntSeguro(extrairValor(opJson, "\"modLog\"\\s*:\\s*(-?\\d+)"));
            int mCar = parseIntSeguro(extrairValor(opJson, "\"modCar\"\\s*:\\s*(-?\\d+)"));
            int mEst = parseIntSeguro(extrairValor(opJson, "\"modEst\"\\s*:\\s*(-?\\d+)"));
            int mCor = parseIntSeguro(extrairValor(opJson, "\"modCor\"\\s*:\\s*(-?\\d+)"));
            int mPis = parseIntSeguro(extrairValor(opJson, "\"modPis\"\\s*:\\s*(-?\\d+)"));
            int mRep = parseIntSeguro(extrairValor(opJson, "\"modRep\"\\s*:\\s*(-?\\d+)"));
            
            String npcNome = extrairValor(opJson, "\"npc\"\\s*:\\s*\"([^\"]*)\"");
            int mRel = parseIntSeguro(extrairValor(opJson, "\"modRel\"\\s*:\\s*(-?\\d+)"));
            NPC npc = npcNome.isEmpty() ? null : new NPC(npcNome, "");
            
            op.setConsequencias(mLog, mCar, mEst, mCor, mPis, mRep, npc, mRel); 

            String reqAttr = extrairValor(opJson, "\"reqAttr\"\\s*:\\s*\"([^\"]*)\"");
            int reqVal = parseIntSeguro(extrairValor(opJson, "\"reqVal\"\\s*:\\s*(\\d+)"));
            if (!reqAttr.isEmpty()) {
                op.setRequisito(reqAttr, reqVal);
            }
            
            this.adicionarOpcao(op);
        }
    }

    private String sanitizarTexto(String texto) {
        if (texto == null) return "";
        return texto.replace("\\\\n", "\n").replace("\\n", "\n");
    }

    private String extrairValor(String texto, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(texto);
        return matcher.find() ? matcher.group(1) : "";
    }

    private int parseIntSeguro(String val) {
        return (val == null || val.isEmpty()) ? 0 : Integer.parseInt(val);
    }
}