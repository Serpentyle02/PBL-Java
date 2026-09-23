/**
 * Modelo que representa uma escolha dentro de uma cena.
 * Contém os requisitos para liberação e os impactos causados nos atributos.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class Opcao {
    private String texto; // Descrição textual apresentada ao jogador
    private int idProximaCena; // ID da cena destino
    private String requisitoAtributo; // Nome da regra/atributo exigido
    private int valorRequisito; // Valor mínimo do requisito
    
    // Modificadores numéricos aplicados aos atributos do jogador
    private int modLog, modCar, modEst, modCor, modPis, modRep, modRel;
    private NPC npcAlvo; // NPC afetado pela decisão

    /**
     * Construtor primário da Opção.
     */
    public Opcao(String texto, int idProximaCena) {
        this.texto = texto;
        this.idProximaCena = idProximaCena;
    }

    /**
     * Configura as regras de desbloqueio da opção.
     */
    public void setRequisito(String attr, int valor) {
        this.requisitoAtributo = attr;
        this.valorRequisito = valor;
    }

    /**
     * Registra os impactos/consequências causados no Protagonista.
     */
    public void setConsequencias(int log, int car, int est, int cor, int pis, int rep, NPC npc, int rel) {
        this.modLog = log; this.modCar = car; this.modEst = est; 
        this.modCor = cor; this.modPis = pis; this.modRep = rep; 
        this.npcAlvo = npc; this.modRel = rel;
    }

    /**
     * Valida se o Protagonista preenche os requisitos necessários para visualizar esta opção.
     * @param p Instância do jogador.
     * @return true se liberada; false caso contrário.
     */
    public boolean verificarCondicao(Protagonista p) {
        if (requisitoAtributo == null || requisitoAtributo.isEmpty()) return true; // Sem restrição
        
        // Entidades para consulta de afinidade
        NPC luiza = new NPC("Luiza", "Aliada");
        NPC beto = new NPC("Beto", "Rival");

        // Validação de Pistas e Atributos Diretos
        if (requisitoAtributo.equals("Pistas")) return p.getPistas() >= valorRequisito;
        if (requisitoAtributo.equals("PistasBaixas")) return p.getPistas() < valorRequisito;
        
        if (requisitoAtributo.equals("CoragemAlta")) return p.getCoragem() >= valorRequisito;
        if (requisitoAtributo.equals("CoragemBaixa")) return p.getCoragem() < valorRequisito;
        
        if (requisitoAtributo.equals("EstresseAlto")) return p.getEstresse() >= valorRequisito;
        if (requisitoAtributo.equals("EstresseBaixo")) return p.getEstresse() <= valorRequisito;
        
        // Validação de Reputação Acadêmica Global
        if (requisitoAtributo.equals("ReputacaoAlta")) return p.getReputacaoGlobal() >= valorRequisito;
        if (requisitoAtributo.equals("ReputacaoBaixa")) return p.getReputacaoGlobal() < valorRequisito;
        
        // Condição especial de falha no flagrante (Cena 15)
        if (requisitoAtributo.equals("FlagranteFalha")) return p.getEstresse() > 6 && p.getCoragem() < 2;
        
        // Validação de Relacionamento/Confiança
        if (requisitoAtributo.equals("ConfiancaAlta")) return p.getRelacionamento(luiza) > 0;
        if (requisitoAtributo.equals("ConfiancaBaixa")) return p.getRelacionamento(luiza) <= 0;
        
        return true; // Por padrão libera a opção
    }

    /**
     * Aplica as alterações de atributos no jogador ao selecionar esta opção.
     */
    public void aplicarConsequencias(Protagonista p) {
        p.alterarLogica(modLog); // Soma/subtrai lógica
        p.alterarCarisma(modCar); // Soma/subtrai carisma
        p.alterarEstresse(modEst); // Soma/subtrai estresse
        p.alterarCoragem(modCor); // Soma/subtrai coragem
        p.alterarPistas(modPis); // Soma/subtrai pistas
        p.alterarReputacaoGlobal(modRep); // Soma/subtrai reputação
        if (npcAlvo != null) p.alterarRelacionamento(npcAlvo, modRel); // Atualiza afinidade com NPC
    }

    // Getters dos dados da opção
    public String getTexto() { return texto; }
    public int getIdProximaCena() { return idProximaCena; }
}