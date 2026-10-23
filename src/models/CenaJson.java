package models;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementação concreta de {@link CenaBase} responsável por carregar e interpretar
 * as cenas da narrativa a partir dos arquivos JSON de roteiro ({@code Roteiro_<idioma>.json}
 * ou {@code Roteiro.json}).
 * Utiliza um parser nativo baseado em varredura de chaves e Expressões Regulares (Regex),
 * dispensando o uso de bibliotecas externas.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public class CenaJson extends CenaBase {

    /**
     * Construtor padrão.
     */
    public CenaJson() {
        super();
    }

    /**
     * Interpreta um bloco JSON correspondente a uma única cena, extraindo seu cabeçalho
     * (id, titulo, texto, isFinal) e a lista de objetos do array "opcoes".
     *
     * @param bloco String contendo o objeto JSON bruto da cena.
     */
    @Override
    public void carregarDados(String bloco) {
        String cabecalho = bloco;
        // Separa o cabeçalho da cena do conteúdo do array "opcoes" para evitar colisões na chave "texto"
        Matcher m = Pattern.compile("\"opcoes\"\\s*:\\s*\\[").matcher(bloco);
        String trechoOpcoes = "";
        if (m.find()) {
            cabecalho = bloco.substring(0, m.start());
            trechoOpcoes = bloco.substring(m.end());
        }

        this.id = extrairInt(cabecalho, "id", -1);
        this.titulo = extrairString(cabecalho, "titulo");
        this.texto = extrairString(cabecalho, "texto");
        this.isFinal = cabecalho.matches("(?s).*\"isFinal\"\\s*:\\s*true.*");
        this.opcoes = extrairOpcoes(trechoOpcoes);
    }

    /**
     * Carrega as cenas do roteiro no idioma padrão ({@code pt_BR}).
     *
     * @return Lista de cenas carregadas e ordenadas por ID.
     */
    public static List<CenaJson> carregarCenasDoArquivo() {
        return carregarCenasDoArquivo("pt_BR");
    }

    /**
     * Localiza o arquivo {@code Roteiro_<idiomaAtual>.json} (com fallback para {@code Roteiro.json}),
     * lê todo o seu conteúdo em UTF-8, converte cada objeto JSON em uma instância de {@link CenaJson}
     * e devolve a lista de cenas ordenada pelo ID.
     *
     * @param idiomaAtual Código do idioma (ex.: {@code "pt_BR"}, {@code "en_US"}, {@code "es_ES"}).
     * @return Lista de cenas carregadas do roteiro.
     */
    public static List<CenaJson> carregarCenasDoArquivo(String idiomaAtual) {
        List<CenaJson> cenas = new ArrayList<>();
        String codigo = (idiomaAtual == null || idiomaAtual.trim().isEmpty()) ? "pt_BR" : idiomaAtual.trim();
        String nomeArquivo = "Roteiro_" + codigo + ".json";

        Path caminho = localizarArquivoRoteiro(nomeArquivo);
        if (caminho == null) {
            caminho = localizarArquivoRoteiro("Roteiro.json");
        }
        if (caminho == null) {
            return cenas;
        }
        try {
            String json = new String(Files.readAllBytes(caminho), StandardCharsets.UTF_8);
            for (String bloco : extrairObjetos(json)) {
                CenaJson cena = new CenaJson();
                cena.carregarDados(bloco);
                if (cena.getId() != -1) cenas.add(cena);
            }
            // Garante a ordenação crescente pelo identificador da cena
            cenas.sort(Comparator.comparingInt(CenaBase::getId));
        } catch (IOException ignored) {
            // Retorna lista vazia caso ocorra falha de leitura; o Controller notifica a View
        }
        return cenas;
    }

    /**
     * Carrega as cenas do roteiro no idioma informado e as organiza em um {@link Map}
     * indexado pelo ID da cena, preservando a ordem crescente de IDs e o polimorfismo com {@link CenaBase}.
     *
     * @param idiomaAtual Código do idioma (ex.: {@code "pt_BR"}, {@code "en_US"}, {@code "es_ES"}).
     * @return Mapa associando o ID de cada cena à sua instância de {@link CenaBase}.
     */
    public static Map<Integer, CenaBase> carregarMapaCenasDoArquivo(String idiomaAtual) {
        Map<Integer, CenaBase> mapa = new LinkedHashMap<>();
        for (CenaJson cena : carregarCenasDoArquivo(idiomaAtual)) {
            mapa.put(cena.getId(), cena);
        }
        return mapa;
    }

    /**
     * Varre uma string JSON e extrai todos os objetos de primeiro nível ({@code {...}}),
     * controlando o aninhamento de chaves e ignorando chaves que estejam dentro de strings
     * ou precedidas por caractere de escape ({@code \}).
     *
     * @param conteudo Trecho JSON contendo uma sequência de objetos.
     * @return Lista com o código JSON individual de cada objeto encontrado.
     */
    private static List<String> extrairObjetos(String conteudo) {
        List<String> objetos = new ArrayList<>();
        int nivel = 0, inicio = -1;
        boolean aspas = false, escape = false;

        for (int i = 0; i < conteudo.length(); i++) {
            char c = conteudo.charAt(i);
            if (escape) { escape = false; continue; }
            if (c == '\\') { escape = true; continue; }
            if (c == '"') { aspas = !aspas; continue; }
            if (aspas) continue; // Ignora chaves '{' ou '}' dentro de textos

            if (c == '{') {
                if (nivel == 0) inicio = i;
                nivel++;
            } else if (c == '}') {
                nivel--;
                if (nivel < 0) break; // Atingiu o fechamento do objeto pai
                if (nivel == 0 && inicio != -1) {
                    objetos.add(conteudo.substring(inicio, i + 1));
                    inicio = -1;
                }
            }
        }
        return objetos;
    }

    /**
     * Converte o trecho do array "opcoes" de uma cena em uma lista de objetos {@link Opcao},
     * preenchendo seus modificadores de atributos, NPC afetado e requisitos.
     */
    private static List<Opcao> extrairOpcoes(String trecho) {
        List<Opcao> lista = new ArrayList<>();
        for (String item : extrairObjetos(trecho)) {
            String txt = extrairString(item, "texto");
            if (txt.isEmpty()) continue;

            Opcao op = new Opcao(txt, extrairInt(item, "idProximaCena", -1));
            op.setConsequencias(
                    extrairInt(item, "modLog", 0), extrairInt(item, "modCar", 0),
                    extrairInt(item, "modEst", 0), extrairInt(item, "modCor", 0),
                    extrairInt(item, "modPis", 0), extrairInt(item, "modRep", 0),
                    NPC.obter(extrairString(item, "npc")), extrairInt(item, "modRel", 0));
            op.setRequisito(extrairString(item, "reqAttr"), extrairInt(item, "reqVal", 0));
            lista.add(op);
        }
        return lista;
    }

    /**
     * Extrai o valor textual de uma chave JSON via Regex, tratando sequências de escape.
     */
    private static String extrairString(String json, String chave) {
        Matcher m = Pattern.compile("\"" + chave + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(json);
        return m.find() ? desescapar(m.group(1)) : "";
    }

    /**
     * Extrai o valor numérico inteiro de uma chave JSON via Regex.
     */
    private static int extrairInt(String json, String chave, int padrao) {
        Matcher m = Pattern.compile("\"" + chave + "\"\\s*:\\s*([+-]?\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : padrao;
    }

    /**
     * Converte sequências escapadas do JSON (como \n, \t, \", \\ e unicode hexadecimal) em seus caracteres reais.
     */
    private static String desescapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': break;
                    case 'u':
                        if (i + 4 < s.length()) {
                            sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                            i += 4;
                        }
                        break;
                    default: sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Procura o arquivo de roteiro nos caminhos padrão do projeto ou recursivamente a partir do diretório atual.
     */
    private static Path localizarArquivoRoteiro(String nomeArquivo) {
        String[] tentativas = {
                "src/jsonFiles/" + nomeArquivo,
                "jsonFiles/" + nomeArquivo,
                "../src/jsonFiles/" + nomeArquivo,
                nomeArquivo
        };
        for (String t : tentativas) {
            Path p = Paths.get(t);
            if (Files.exists(p)) return p;
        }
        return buscarRecursivo(new File("."), nomeArquivo);
    }

    /**
     * Busca recursivamente pelo arquivo do roteiro ignorando pastas ocultas (iniciadas por ponto) e {@code bin}.
     */
    private static Path buscarRecursivo(File dir, String nomeArquivo) {
        File[] arquivos = dir.listFiles();
        if (arquivos == null) return null;
        for (File f : arquivos) {
            if (f.isDirectory() && !f.getName().startsWith(".") && !f.getName().equals("bin")) {
                Path achado = buscarRecursivo(f, nomeArquivo);
                if (achado != null) return achado;
            } else if (f.isFile() && f.getName().equalsIgnoreCase(nomeArquivo)) {
                return f.toPath();
            }
        }
        return null;
    }

    public List<Opcao> getOpcoes() { return opcoes; }
}
