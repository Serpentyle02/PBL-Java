package models;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CenaJson extends CenaBase {

    private List<Opcao> opcoes = new ArrayList<>();

    public CenaJson() {
        super();
    }

    @Override
    public void carregarDados(String bloco) {
        String cabecalho = bloco;
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

    public static List<CenaJson> carregarCenasDoArquivo() {
        List<CenaJson> cenas = new ArrayList<>();
        Path caminho = localizarArquivoRoteiro();
        if (caminho == null) {
            System.err.println("\n[ERRO] Arquivo Roteiro.json nao encontrado!");
            return cenas;
        }
        try {
            String json = new String(Files.readAllBytes(caminho), StandardCharsets.UTF_8);
            for (String bloco : extrairObjetos(json)) {
                CenaJson cena = new CenaJson();
                cena.carregarDados(bloco);
                if (cena.getId() != -1) cenas.add(cena);
            }
            cenas.sort(Comparator.comparingInt(CenaBase::getId));
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao ler Roteiro.json: " + e.getMessage());
        }
        return cenas;
    }

    /** Extrai os objetos {...} de nível 0, respeitando strings e escapes. */
    private static List<String> extrairObjetos(String conteudo) {
        List<String> objetos = new ArrayList<>();
        int nivel = 0, inicio = -1;
        boolean aspas = false, escape = false;

        for (int i = 0; i < conteudo.length(); i++) {
            char c = conteudo.charAt(i);
            if (escape) { escape = false; continue; }
            if (c == '\\') { escape = true; continue; }
            if (c == '"') { aspas = !aspas; continue; }
            if (aspas) continue;

            if (c == '{') {
                if (nivel == 0) inicio = i;
                nivel++;
            } else if (c == '}') {
                nivel--;
                if (nivel < 0) break; // fechamento do objeto pai
                if (nivel == 0 && inicio != -1) {
                    objetos.add(conteudo.substring(inicio, i + 1));
                    inicio = -1;
                }
            }
        }
        return objetos;
    }

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
                    extrairString(item, "npc"), extrairInt(item, "modRel", 0));
            op.setRequisito(extrairString(item, "reqAttr"), extrairInt(item, "reqVal", 0));
            lista.add(op);
        }
        return lista;
    }

    private static String extrairString(String json, String chave) {
        Matcher m = Pattern.compile("\"" + chave + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(json);
        return m.find() ? desescapar(m.group(1)) : "";
    }

    private static int extrairInt(String json, String chave, int padrao) {
        Matcher m = Pattern.compile("\"" + chave + "\"\\s*:\\s*([+-]?\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : padrao;
    }

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

    private static Path localizarArquivoRoteiro() {
        String[] tentativas = {
            "src/jsonFiles/Roteiro.json", "jsonFiles/Roteiro.json", "Roteiro.json"
        };
        for (String t : tentativas) {
            Path p = Paths.get(t);
            if (Files.exists(p)) return p;
        }
        return buscarRecursivo(new File("."));
    }

    private static Path buscarRecursivo(File dir) {
        File[] arquivos = dir.listFiles();
        if (arquivos == null) return null;
        for (File f : arquivos) {
            if (f.isDirectory() && !f.getName().startsWith(".")) {
                Path achado = buscarRecursivo(f);
                if (achado != null) return achado;
            } else if (f.getName().equalsIgnoreCase("roteiro.json")) {
                return f.toPath();
            }
        }
        return null;
    }

    public List<Opcao> getOpcoes() { return opcoes; }
}