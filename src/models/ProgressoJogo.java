package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Container de estado que representa tanto os dados de um slot de salvamento
 * (protagonista, cena atual, data/hora e resumo) quanto as configurações e
 * conquistas globais do jogador (velocidade do texto, auto-save, dicas,
 * idioma preferido, conteúdos desbloqueados na galeria e finais alcançados).
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class ProgressoJogo {

    // Dados específicos de um Slot de Save (1 a 3)
    private int slotId;
    private int idCenaAtual;
    private String tituloCena;
    private String dataHora;
    private String resumoProgressao;
    private Protagonista protagonista;

    // Preferências do usuário e registros globais (persistidos em dados_globais.json)
    private int velocidadeTextoMs = 20;
    private boolean autoSaveAtivo = true;
    private boolean exibirDicas = true;
    private String idiomaPref = "pt_BR";
    private final List<String> desbloqueaveis = new ArrayList<>();
    private final List<String> finais = new ArrayList<>();

    /**
     * Construtor utilizado ao salvar ou carregar um slot de partida.
     *
     * @param slotId       Número do slot (1 a 3).
     * @param protagonista Instância do protagonista com seus atributos e relações.
     * @param idCenaAtual  ID da cena em que o jogo foi salvo.
     * @param tituloCena   Título do capítulo/cena em que o jogo foi salvo.
     * @param dataHora     Carimbo de data e hora do salvamento.
     * @param resumo       Trecho inicial da cena para identificação visual do save.
     */
    public ProgressoJogo(int slotId, Protagonista protagonista, int idCenaAtual, String tituloCena, String dataHora, String resumo) {
        this.slotId = slotId;
        this.protagonista = protagonista;
        this.idCenaAtual = idCenaAtual;
        this.tituloCena = tituloCena;
        this.dataHora = dataHora;
        this.resumoProgressao = resumo;
    }

    /**
     * Construtor padrão utilizado para instanciar os dados globais do jogo.
     */
    public ProgressoJogo() {}

    /**
     * Registra um novo item desbloqueável na galeria caso ainda não tenha sido obtido.
     *
     * @param item Nome da conquista ou dossiê desbloqueado.
     * @return {@code true} se o item era inédito e foi adicionado; {@code false} se já constava na lista.
     */
    public boolean adicionarDesbloqueavel(String item) {
        return !desbloqueaveis.contains(item) && desbloqueaveis.add(item);
    }

    /**
     * Registra um novo final alcançado pelo jogador caso ainda não tenha sido descoberto.
     *
     * @param tituloFinal Título descritivo do final.
     * @return {@code true} se o final era inédito e foi registrado; {@code false} caso contrário.
     */
    public boolean adicionarFinal(String tituloFinal) {
        return !finais.contains(tituloFinal) && finais.add(tituloFinal);
    }

    // Métodos seletores e modificadores (Getters e Setters)
    public int getSlotId() { return slotId; }
    public int getIdCenaAtual() { return idCenaAtual; }
    public String getTituloCena() { return tituloCena; }
    public String getDataHora() { return dataHora; }
    public String getResumoProgressao() { return resumoProgressao; }
    public Protagonista getProtagonista() { return protagonista; }

    public int getVelocidadeTextoMs() { return velocidadeTextoMs; }
    public void setVelocidadeTextoMs(int v) { this.velocidadeTextoMs = v; }

    public boolean isAutoSaveAtivo() { return autoSaveAtivo; }
    public void setAutoSaveAtivo(boolean v) { this.autoSaveAtivo = v; }

    public boolean isExibirDicas() { return exibirDicas; }
    public void setExibirDicas(boolean v) { this.exibirDicas = v; }

    public String getIdiomaPref() { return idiomaPref; }
    public void setIdiomaPref(String idiomaPref) {
        this.idiomaPref = (idiomaPref == null || idiomaPref.trim().isEmpty()) ? "pt_BR" : idiomaPref.trim();
    }

    public List<String> getDesbloqueaveis() { return desbloqueaveis; }
    public List<String> getFinais() { return finais; }
}
