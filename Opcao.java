public class Opcao {
    private String texto;
    private int idProximaCena;
    private String requisitoAtributo;
    private int valorRequisito;
    
    private int modLog, modCar, modEst, modPis, modRep, modRel;
    private NPC npcAlvo;

    public Opcao(String texto, int idProximaCena) {
        this.texto = texto;
        this.idProximaCena = idProximaCena;
    }

    public void setRequisito(String attr, int valor) {
        this.requisitoAtributo = attr;
        this.valorRequisito = valor;
    }

    public void setConsequencias(int log, int car, int est, int pis, int rep, NPC npc, int rel) {
        this.modLog = log; this.modCar = car; this.modEst = est; this.modPis = pis;
        this.modRep = rep; this.npcAlvo = npc; this.modRel = rel;
    }

    public boolean verificarCondicao(Protagonista p) {
        if (requisitoAtributo == null || requisitoAtributo.isEmpty()) return true;
        
        NPC alice = new NPC("Alice", "Aliada");
        NPC beto = new NPC("Beto", "Rival");

        if (requisitoAtributo.equals("Pistas")) return p.getPistas() >= valorRequisito;
        if (requisitoAtributo.equals("ConfiancaAlta")) return p.getRelacionamento(alice) > 0;
        if (requisitoAtributo.equals("ConfiancaBaixa")) return p.getRelacionamento(alice) <= 0;
        if (requisitoAtributo.equals("AcusarBeto")) return p.getPistas() < 2 && p.getRelacionamento(beto) >= 1;
        
        // Condições de Estresse
        if (requisitoAtributo.equals("EstresseAlto")) return p.getEstresse() >= valorRequisito;
        if (requisitoAtributo.equals("EstresseBaixo")) return p.getEstresse() < valorRequisito;
        
        return true;
    }

    public void aplicarConsequencias(Protagonista p) {
        p.alterarLogica(modLog); 
        p.alterarCarisma(modCar);
        p.alterarEstresse(modEst); 
        p.alterarPistas(modPis);
        p.alterarReputacaoGlobal(modRep);
        if (npcAlvo != null) p.alterarRelacionamento(npcAlvo, modRel);
    }

    public String getTexto() { return texto; }
    public int getIdProximaCena() { return idProximaCena; }
}