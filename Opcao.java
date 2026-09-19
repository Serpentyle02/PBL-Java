public class Opcao {
    private String texto;
    private int idProximaCena;
    private String requisitoAtributo;
    private int valorRequisito;
    
    private int modLog, modCar, modEst, modCor, modPis, modRep, modRel;
    private NPC npcAlvo;

    public Opcao(String texto, int idProximaCena) {
        this.texto = texto;
        this.idProximaCena = idProximaCena;
    }

    public void setRequisito(String attr, int valor) {
        this.requisitoAtributo = attr;
        this.valorRequisito = valor;
    }

    public void setConsequencias(int log, int car, int est, int cor, int pis, int rep, NPC npc, int rel) {
        this.modLog = log; this.modCar = car; this.modEst = est; 
        this.modCor = cor; this.modPis = pis; this.modRep = rep; 
        this.npcAlvo = npc; this.modRel = rel;
    }

    public boolean verificarCondicao(Protagonista p) {
        if (requisitoAtributo == null || requisitoAtributo.isEmpty()) return true;
        
        NPC luiza = new NPC("Luiza", "Aliada");
        NPC beto = new NPC("Beto", "Rival");

        // Recursos e Atributos
        if (requisitoAtributo.equals("Pistas")) return p.getPistas() >= valorRequisito;
        if (requisitoAtributo.equals("PistasBaixas")) return p.getPistas() < valorRequisito;
        
        if (requisitoAtributo.equals("CoragemAlta")) return p.getCoragem() >= valorRequisito;
        if (requisitoAtributo.equals("CoragemBaixa")) return p.getCoragem() < valorRequisito;
        
        if (requisitoAtributo.equals("EstresseAlto")) return p.getEstresse() >= valorRequisito;
        if (requisitoAtributo.equals("EstresseBaixo")) return p.getEstresse() <= valorRequisito;
        
        // REPUTAÇÃO GERAL (Impacto Direto na História)
        if (requisitoAtributo.equals("ReputacaoAlta")) return p.getReputacaoGlobal() >= valorRequisito;
        if (requisitoAtributo.equals("ReputacaoBaixa")) return p.getReputacaoGlobal() < valorRequisito;
        
        // Condição do Flagrante (Cena 15)[cite: 5]
        if (requisitoAtributo.equals("FlagranteFalha")) return p.getEstresse() > 6 && p.getCoragem() < 2;
        
        // Relacionamentos[cite: 5]
        if (requisitoAtributo.equals("ConfiancaAlta")) return p.getRelacionamento(luiza) > 0;
        if (requisitoAtributo.equals("ConfiancaBaixa")) return p.getRelacionamento(luiza) <= 0;
        
        return true;
    }

    public void aplicarConsequencias(Protagonista p) {
        p.alterarLogica(modLog); 
        p.alterarCarisma(modCar);
        p.alterarEstresse(modEst); 
        p.alterarCoragem(modCor);
        p.alterarPistas(modPis);
        p.alterarReputacaoGlobal(modRep);
        if (npcAlvo != null) p.alterarRelacionamento(npcAlvo, modRel);
    }

    public String getTexto() { return texto; }
    public int getIdProximaCena() { return idProximaCena; }
}