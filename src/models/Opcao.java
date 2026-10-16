package models;

public class Opcao {

    private final String texto;
    private final int idProximaCena;

    private int modLog, modCar, modEst, modCor, modPis, modRep;
    private String npc = "";
    private int modRel;
    private String reqAttr = "";
    private int reqVal;

    public Opcao(String texto, int idProximaCena) {
        this.texto = texto;
        this.idProximaCena = idProximaCena;
    }

    public void setConsequencias(int log, int car, int est, int cor, int pis, int rep, String npc, int rel) {
        this.modLog = log;
        this.modCar = car;
        this.modEst = est;
        this.modCor = cor;
        this.modPis = pis;
        this.modRep = rep;
        this.npc = (npc != null) ? npc : "";
        this.modRel = rel;
    }

    public void setRequisito(String attr, int valor) {
        this.reqAttr = (attr != null) ? attr : "";
        this.reqVal = valor;
    }

    /** Verifica se o protagonista cumpre o requisito da opção. */
    public boolean verificarCondicao(Protagonista p) {
        if (p == null || reqAttr.isEmpty()) return true;
        switch (reqAttr) {
            case "Pistas":          return p.getPistas() >= reqVal;
            case "CoragemAlta":     return p.getCoragem() >= reqVal;
            case "EstresseBaixo":   return p.getEstresse() < reqVal;
            case "ReputacaoAlta":   return p.getReputacao() >= reqVal;
            case "ReputacaoBaixa":  return p.getReputacao() < reqVal;
            // Complemento de EstresseBaixo(<6) e CoragemAlta(>=3): garante que a Cena 15 nunca trave
            case "FlagranteFalha":  return p.getEstresse() >= 6 && p.getCoragem() < 3;
            default:                return true;
        }
    }

    /** Aplica os efeitos ao protagonista e devolve um resumo textual. */
    public String aplicarConsequencias(Protagonista p) {
        if (p == null) return "";
        StringBuilder sb = new StringBuilder();

        if (modLog != 0) { p.setLogica(p.getLogica() + modLog); sb.append(fmt("Lógica", modLog)); }
        if (modCar != 0) { p.setCarisma(p.getCarisma() + modCar); sb.append(fmt("Carisma", modCar)); }
        if (modEst != 0) { p.setEstresse(p.getEstresse() + modEst); sb.append(fmt("Estresse", modEst)); }
        if (modCor != 0) { p.setCoragem(p.getCoragem() + modCor); sb.append(fmt("Coragem", modCor)); }
        if (modPis != 0) { p.setPistas(p.getPistas() + modPis); sb.append(fmt("Pistas", modPis)); }
        if (modRep != 0) { p.setReputacao(p.getReputacao() + modRep); sb.append(fmt("Reputação", modRep)); }
        if (!npc.isEmpty() && modRel != 0) {
            p.atualizarRelacionamento(npc, modRel);
            sb.append(fmt("Relação com " + npc, modRel));
        }
        return sb.toString().trim();
    }

    private String fmt(String nome, int v) {
        return "[" + nome + ": " + (v > 0 ? "+" : "") + v + "] ";
    }

    public String getTexto() { return texto; }
    public int getIdProximaCena() { return idProximaCena; }
}