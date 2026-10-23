package models;

/**
 * Representa uma opção de decisão disponível para o jogador dentro de uma cena.
 * Encapsula o texto da escolha, o ID da cena de destino, os requisitos de acesso
 * e os modificadores de atributos/relacionamentos resultantes da decisão.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class Opcao {

    // Texto descritivo da ação exibido no menu de escolhas
    private final String texto;
    // Identificador numérico da cena para a qual esta opção redireciona
    private final int idProximaCena;

    // Modificadores (deltas) aplicados aos atributos do protagonista ao escolher esta opção
    private int modLog, modCar, modEst, modCor, modPis, modRep;
    // NPC afetado pela escolha (nulo se nenhum) e o delta de afinidade correspondente
    private NPC npc;
    private int modRel;

    // Condição necessária para desbloquear a opção (ex.: "Pistas", "CoragemAlta") e seu valor alvo
    private String reqAttr = "";
    private int reqVal;

    /**
     * Construtor da classe Opcao.
     *
     * @param texto         Texto exibido ao jogador.
     * @param idProximaCena ID da cena de destino caso a opção seja escolhida.
     */
    public Opcao(String texto, int idProximaCena) {
        this.texto = texto;
        this.idProximaCena = idProximaCena;
    }

    /**
     * Define todos os efeitos (consequências) que esta opção causará no protagonista.
     *
     * @param log Modificador de Lógica.
     * @param car Modificador de Carisma.
     * @param est Modificador de Estresse.
     * @param cor Modificador de Coragem.
     * @param pis Modificador de Pistas.
     * @param rep Modificador de Reputação.
     * @param npc NPC cujo relacionamento será alterado (nulo se nenhum).
     * @param rel Modificador de afinidade com o NPC.
     */
    public void setConsequencias(int log, int car, int est, int cor, int pis, int rep, NPC npc, int rel) {
        this.modLog = log;
        this.modCar = car;
        this.modEst = est;
        this.modCor = cor;
        this.modPis = pis;
        this.modRep = rep;
        this.npc = npc;
        this.modRel = rel;
    }

    /**
     * Define o requisito de atributo necessário para que a opção possa ser selecionada.
     *
     * @param attr  Identificador do requisito no roteiro (ex.: "Pistas", "CoragemAlta").
     * @param valor Valor mínimo ou limite exigido pelo requisito.
     */
    public void setRequisito(String attr, int valor) {
        this.reqAttr = (attr != null) ? attr : "";
        this.reqVal = valor;
    }

    /**
     * Verifica se o protagonista cumpre a condição exigida por esta opção.
     * Opções sem requisito ({@code reqAttr} vazio) estão sempre liberadas.
     *
     * @param p Instância atual do Protagonista.
     * @return {@code true} se a opção puder ser escolhida; {@code false} se estiver bloqueada.
     */
    public boolean verificarCondicao(Protagonista p) {
        if (p == null || reqAttr.isEmpty()) return true;
        switch (reqAttr) {
            case "Pistas":          return p.getPistas() >= reqVal;
            case "CoragemAlta":     return p.getCoragem() >= reqVal;
            case "EstresseBaixo":   return p.getEstresse() < reqVal;
            case "ReputacaoAlta":   return p.getReputacao() >= reqVal;
            case "ReputacaoBaixa":  return p.getReputacao() < reqVal;
            // Falha no flagrante (Cena 15): pressão alta com pouca coragem. Os limites foram calibrados
            // com as combinações reais de atributos do roteiro, tornando o Final 4 alcançável.
            case "FlagranteFalha":  return p.getEstresse() >= 4 && p.getCoragem() < 5;
            default:                return true;
        }
    }

    /**
     * Aplica todas as consequências (modificadores de atributos e afinidade com NPC)
     * ao protagonista e constrói um resumo textual das alterações ocorridas.
     *
     * @param p Instância atual do Protagonista.
     * @return String formatada com o impacto nos status (ex.: "[Lógica: +1] [Pistas: +1]"),
     *         ou vazio caso nenhum atributo tenha sido modificado.
     */
    public String aplicarConsequencias(Protagonista p) {
        if (p == null) return "";
        StringBuilder sb = new StringBuilder();

        if (modLog != 0) { p.setLogica(p.getLogica() + modLog); sb.append(fmt("Lógica", modLog)); }
        if (modCar != 0) { p.setCarisma(p.getCarisma() + modCar); sb.append(fmt("Carisma", modCar)); }
        if (modEst != 0) { p.setEstresse(p.getEstresse() + modEst); sb.append(fmt("Estresse", modEst)); }
        if (modCor != 0) { p.setCoragem(p.getCoragem() + modCor); sb.append(fmt("Coragem", modCor)); }
        if (modPis != 0) { p.setPistas(p.getPistas() + modPis); sb.append(fmt("Pistas", modPis)); }
        if (modRep != 0) { p.setReputacao(p.getReputacao() + modRep); sb.append(fmt("Reputação", modRep)); }
        if (npc != null && modRel != 0) {
            p.atualizarRelacionamento(npc, modRel);
            sb.append(fmt("Relação com " + npc.getNome(), modRel));
        }
        return sb.toString().trim();
    }

    /**
     * Formata individualmente a mudança de um atributo com sinal explícito (+ ou -).
     */
    private String fmt(String nome, int v) {
        return "[" + nome + ": " + (v > 0 ? "+" : "") + v + "] ";
    }

    public String getTexto() { return texto; }
    public int getIdProximaCena() { return idProximaCena; }
    public NPC getNpc() { return npc; }
}
