package models;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gerenciador de internacionalização (i18n) dos textos de interface do jogo.
 * Carrega dinamicamente os arquivos {@code Interface_<idioma>.json} (ex.: {@code pt_BR},
 * {@code en_US}, {@code es_ES}) em UTF-8 sem bibliotecas externas.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public final class IdiomaManager {

    private String idiomaAtual;
    private final Map<String, String> textosInterface = new HashMap<>();

    /**
     * Construtor padrão. Inicializa o gerenciador com o idioma Português do Brasil ({@code pt_BR}).
     */
    public IdiomaManager() {
        setIdioma("pt_BR");
    }

    /**
     * Obtém o código do idioma ativo no momento.
     *
     * @return Código do idioma (ex.: {@code "pt_BR"}, {@code "en_US"} ou {@code "es_ES"}).
     */
    public String getIdiomaAtual() {
        return idiomaAtual;
    }

    /**
     * Define o idioma ativo e recarrega o dicionário de mensagens da interface.
     * Caso o código informado seja inválido ou nulo, utiliza {@code "pt_BR"} como fallback.
     *
     * @param idioma Código do idioma desejado.
     */
    public void setIdioma(String idioma) {
        String codigo = (idioma == null) ? "" : idioma.trim();
        if (!codigo.equals("en_US") && !codigo.equals("es_ES") && !codigo.equals("pt_BR")) {
            codigo = "pt_BR";
        }
        this.idiomaAtual = codigo;
        carregarTextosInterface();
    }

    /**
     * Lê o arquivo {@code Interface_<idiomaAtual>.json} e popula o mapa {@code textosInterface}.
     */
    private void carregarTextosInterface() {
        textosInterface.clear();
        String nomeArquivo = "Interface_" + idiomaAtual + ".json";
        Path caminho = localizarArquivo(nomeArquivo);
        if (caminho == null && !"pt_BR".equals(idiomaAtual)) {
            caminho = localizarArquivo("Interface_pt_BR.json");
        }

        if (caminho != null) {
            try {
                String json = new String(Files.readAllBytes(caminho), StandardCharsets.UTF_8);
                Matcher m = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(json);
                while (m.find()) {
                    textosInterface.put(desescapar(m.group(1)), desescapar(m.group(2)));
                }
            } catch (IOException ignored) {
                // Em caso de falha de leitura, get(chave) retorna a própria chave como fallback seguro
            }
        }
    }

    /**
     * Converte sequências de escape JSON (\n, \t, \", \\) para seus caracteres reais.
     */
    private static String desescapar(String s) {
        return s.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * Localiza um arquivo JSON de idioma nos diretórios padrão do projeto.
     */
    private Path localizarArquivo(String nomeArquivo) {
        String[] bases = {"src/jsonFiles/", "jsonFiles/", "../src/jsonFiles/", ""};
        for (String base : bases) {
            Path p = Paths.get(base + nomeArquivo);
            if (Files.exists(p)) return p;
        }
        return buscarRecursivo(new File("."), nomeArquivo);
    }

    /**
     * Busca recursivamente pelo arquivo de idioma ignorando pastas ocultas e o diretório {@code bin}.
     */
    private Path buscarRecursivo(File dir, String nomeArquivo) {
        File[] arquivos = dir.listFiles();
        if (arquivos == null) return null;
        for (File arquivo : arquivos) {
            if (arquivo.isDirectory() && !arquivo.getName().startsWith(".") && !arquivo.getName().equals("bin")) {
                Path encontrado = buscarRecursivo(arquivo, nomeArquivo);
                if (encontrado != null) return encontrado;
            } else if (arquivo.isFile() && arquivo.getName().equalsIgnoreCase(nomeArquivo)) {
                return arquivo.toPath();
            }
        }
        return null;
    }

    /**
     * Retorna o texto traduzido associado à chave informada, ou a própria chave caso não exista.
     *
     * @param chave Identificador do texto no arquivo {@code Interface_<idioma>.json}.
     * @return Texto correspondente no idioma atual.
     */
    public String get(String chave) {
        return textosInterface.getOrDefault(chave, chave);
    }
}
