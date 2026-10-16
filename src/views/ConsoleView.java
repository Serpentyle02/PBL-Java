package views;

import models.Protagonista;

import java.io.IOException;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleView {

    private final Scanner scanner = new Scanner(System.in);

    public void exibirMensagem(String msg) {
        System.out.println(msg);
    }

    public void exibirNarrativaComPreferencia(String texto, int velocidadeMs) {
        if (texto == null) return;
        if (velocidadeMs <= 0) {
            System.out.println(texto);
            return;
        }
        for (int i = 0; i < texto.length(); i++) {
            System.out.print(texto.charAt(i));
            System.out.flush();
            try {
                Thread.sleep(velocidadeMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.print(texto.substring(i + 1));
                break;
            }
        }
        System.out.println();
    }

    public void exibirStatusProtagonista(Protagonista p) {
        if (p == null) return;
        System.out.println("==================================================================");
        System.out.printf(" %s | Lóg: %d | Car: %d | Est: %d | Cor: %d | Pistas: %d | Rep: %d%n",
                p.getNome(), p.getLogica(), p.getCarisma(), p.getEstresse(),
                p.getCoragem(), p.getPistas(), p.getReputacao());
        if (!p.getRelacionamentos().isEmpty()) {
            System.out.println(" Relações: " + p.getRelacionamentos());
        }
        System.out.println("==================================================================");
    }

    public String lerEntrada() {
        System.out.print("> ");
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            encerrarPorFimDeEntrada();
            return "0";
        }
    }

    public void pausarParaLeitura() {
        System.out.println("\n[Pressione ENTER para continuar...]");
        try {
            scanner.nextLine();
        } catch (NoSuchElementException e) {
            encerrarPorFimDeEntrada();
        }
    }

    private void encerrarPorFimDeEntrada() {
        System.out.println("\n[Entrada encerrada. Saindo do jogo.]");
        System.exit(0);
    }

    /** Apaga o terminal por completo (inclusive o histórico de rolagem), mostrando só a tela atual. */
    public void limparTela() {
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                return;
            }
        } catch (IOException e) {
            // cai para o método ANSI abaixo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }
}