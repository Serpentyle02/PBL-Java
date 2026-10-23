package models;

/**
 * Representa uma conquista (item desbloqueável da galeria) do jogo, com suporte
 * a múltiplos idiomas ({@code pt_BR}, {@code en_US} e {@code es_ES}).
 * Cada conquista possui título e descrição traduzidos e uma {@link Regra} que decide,
 * a partir do estado da partida, se ela foi atingida.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class Conquista {

    /**
     * Regra de desbloqueio de uma conquista.
     */
    public interface Regra {
        /**
         * Avalia se a conquista foi atingida no estado atual.
         *
         * @param p          Protagonista com os atributos e relacionamentos atuais.
         * @param idCena     ID da cena em exibição.
         * @param cenaFinal  Indica se a cena em exibição é um final.
         * @param global     Dados globais (finais e conquistas já registrados).
         * @return {@code true} se as condições da conquista foram cumpridas.
         */
        boolean atingida(Protagonista p, int idCena, boolean cenaFinal, ProgressoJogo global);
    }

    private final String tituloPt;
    private final String descricaoPt;
    private final String tituloEn;
    private final String descricaoEn;
    private final String tituloEs;
    private final String descricaoEs;
    private final Regra regra;

    /**
     * Construtor padrão com título e descrição em Português.
     *
     * @param titulo    Título canônico da conquista (chave de persistência).
     * @param descricao Descrição da conquista em Português.
     * @param regra     Regra de desbloqueio.
     */
    public Conquista(String titulo, String descricao, Regra regra) {
        this(titulo, descricao, titulo, descricao, titulo, descricao, regra);
    }

    /**
     * Construtor completo com traduções para Português ({@code pt_BR}), Inglês ({@code en_US})
     * e Espanhol ({@code es_ES}).
     *
     * @param tituloPt    Título em Português (também usado como chave única em {@code dados_globais.json}).
     * @param descricaoPt Descrição em Português.
     * @param tituloEn    Título em Inglês.
     * @param descricaoEn Descrição em Inglês.
     * @param tituloEs    Título em Espanhol.
     * @param descricaoEs Descrição em Espanhol.
     * @param regra       Regra de desbloqueio.
     */
    public Conquista(String tituloPt, String descricaoPt,
                     String tituloEn, String descricaoEn,
                     String tituloEs, String descricaoEs,
                     Regra regra) {
        this.tituloPt = tituloPt;
        this.descricaoPt = descricaoPt;
        this.tituloEn = tituloEn;
        this.descricaoEn = descricaoEn;
        this.tituloEs = tituloEs;
        this.descricaoEs = descricaoEs;
        this.regra = regra;
    }

    /**
     * Verifica se as condições desta conquista foram cumpridas no estado informado.
     */
    public boolean foiAtingida(Protagonista p, int idCena, boolean cenaFinal, ProgressoJogo global) {
        return regra.atingida(p, idCena, cenaFinal, global);
    }

    /**
     * Obtém o título canônico (em Português), utilizado como chave única de persistência.
     */
    public String getTitulo() {
        return tituloPt;
    }

    /**
     * Obtém a descrição em Português.
     */
    public String getDescricao() {
        return descricaoPt;
    }

    /**
     * Obtém o título traduzido para o idioma informado ({@code "pt_BR"}, {@code "en_US"} ou {@code "es_ES"}).
     *
     * @param idioma Código do idioma ativo.
     * @return Título da conquista no idioma solicitado.
     */
    public String getTitulo(String idioma) {
        if ("en_US".equals(idioma)) return tituloEn;
        if ("es_ES".equals(idioma)) return tituloEs;
        return tituloPt;
    }

    /**
     * Obtém a descrição traduzida para o idioma informado ({@code "pt_BR"}, {@code "en_US"} ou {@code "es_ES"}).
     *
     * @param idioma Código do idioma ativo.
     * @return Descrição da conquista no idioma solicitado.
     */
    public String getDescricao(String idioma) {
        if ("en_US".equals(idioma)) return descricaoEn;
        if ("es_ES".equals(idioma)) return descricaoEs;
        return descricaoPt;
    }
}
