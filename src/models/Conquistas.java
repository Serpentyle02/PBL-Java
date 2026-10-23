package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Catálogo de todas as conquistas do jogo (com suporte a {@code pt_BR}, {@code en_US} e {@code es_ES})
 * e verificação de desbloqueio.
 * Os limites de atributos foram calibrados com os valores que o roteiro realmente permite alcançar.
 *
 * @author Renan Queiroz & Felipe Pereira
 */
public final class Conquistas {

    // Títulos canônicos que dependem de todas as outras / dos 4 finais (tratadas como meta-conquistas)
    public static final String VETERANO = "Conquista: Veterano da Investigação";
    public static final String MESTRE = "Conquista: Mestre da Investigação";

    // IDs das cenas finais do roteiro
    private static final int FINAL_BODE = 21;
    private static final int FINAL_CUMPLICE = 22;
    private static final int FINAL_EXPULSAO = 23;
    private static final int FINAL_DETETIVE = 201;

    private static final List<Conquista> CATALOGO = new ArrayList<>();

    static {
        // --- Marcos da história ---
        CATALOGO.add(new Conquista(
                "Conquista: Primeiros Passos na Investigação", "Chegar à cena do desaparecimento do código.",
                "Achievement: First Steps in the Investigation", "Reach the scene of the missing code.",
                "Logro: Primeros Pasos en la Investigación", "Llegar a la escena de la desaparición del código.",
                (p, id, fim, g) -> id == 2));
        CATALOGO.add(new Conquista(
                "Dossiê Confidencial: A Testemunha Oculta", "Descobrir a existência de uma testemunha.",
                "Confidential Dossier: The Hidden Witness", "Discover the existence of a witness.",
                "Dossier Confidencial: El Testigo Oculto", "Descubrir la existencia de un testigo.",
                (p, id, fim, g) -> id == 5));

        // --- Atributos ---
        CATALOGO.add(new Conquista(
                "Conquista: Mente Brilhante", "Alcançar Lógica 10 ou mais.",
                "Achievement: Brilliant Mind", "Reach Logic 10 or higher.",
                "Logro: Mente Brillante", "Alcanzar Lógica 10 o más.",
                (p, id, fim, g) -> p.getLogica() >= 10));
        CATALOGO.add(new Conquista(
                "Conquista: Língua de Ouro", "Alcançar Carisma 8 ou mais.",
                "Achievement: Silver Tongue", "Reach Charisma 8 or higher.",
                "Logro: Lengua de Oro", "Alcanzar Carisma 8 o más.",
                (p, id, fim, g) -> p.getCarisma() >= 8));
        CATALOGO.add(new Conquista(
                "Conquista: Sem Medo", "Alcançar Coragem 12 ou mais.",
                "Achievement: Fearless", "Reach Courage 12 or higher.",
                "Logro: Sin Miedo", "Alcanzar Coraje 12 o más.",
                (p, id, fim, g) -> p.getCoragem() >= 12));
        CATALOGO.add(new Conquista(
                "Conquista: Caçador de Pistas", "Reunir 5 pistas.",
                "Achievement: Clue Hunter", "Gather 5 clues.",
                "Logro: Cazador de Pistas", "Reunir las 5 pistas.",
                (p, id, fim, g) -> p.getPistas() >= 5));
        CATALOGO.add(new Conquista(
                "Conquista: No Limite", "Chegar a Estresse 7 ou mais.",
                "Achievement: On the Edge", "Reach Stress 7 or higher.",
                "Logro: Al Límite", "Llegar a Estrés 7 o más.",
                (p, id, fim, g) -> p.getEstresse() >= 7));

        // --- Relacionamentos ---
        CATALOGO.add(new Conquista(
                "Conquista: Aluno Exemplar", "Conquistar a confiança do Prof. Silva.",
                "Achievement: Exemplary Student", "Earn Prof. Silva's trust.",
                "Logro: Alumno Ejemplar", "Ganar la confianza del Prof. Silva.",
                (p, id, fim, g) -> relacao(p, "Prof. Silva") >= 1));
        CATALOGO.add(new Conquista(
                "Conquista: Aliada Hacker", "Ganhar a simpatia de Luiza.",
                "Achievement: Hacker Ally", "Win Luiza's sympathy.",
                "Logro: Aliada Hacker", "Ganar la simpatía de Luiza.",
                (p, id, fim, g) -> relacao(p, "Luiza") >= 2));
        CATALOGO.add(new Conquista(
                "Conquista: Amigo Improvável", "Fazer de Beto um aliado (afinidade 5 ou mais).",
                "Achievement: Unlikely Friend", "Make Beto an ally (affinity 5 or higher).",
                "Logro: Amigo Improbable", "Hacer de Beto un aliado (afinidad 5 o más).",
                (p, id, fim, g) -> relacao(p, "Beto") >= 5));

        // --- Finais ---
        CATALOGO.add(new Conquista(
                "Conquista: Detetive Acadêmico", "Alcançar o final em que a verdade vem à tona.",
                "Achievement: Academic Detective", "Reach the ending where the truth comes to light.",
                "Logro: Detective Académico", "Alcanzar el final en que la verdad sale a la luz.",
                (p, id, fim, g) -> fim && id == FINAL_DETETIVE));
        CATALOGO.add(new Conquista(
                "Conquista: Bode Expiatório", "Alcançar o final em que você paga pelo erro dos outros.",
                "Achievement: Scapegoat", "Reach the ending where you pay for others' mistakes.",
                "Logro: Chivo Expiatorio", "Alcanzar el final en que pagas por el error de otros.",
                (p, id, fim, g) -> fim && id == FINAL_BODE));
        CATALOGO.add(new Conquista(
                "Conquista: Preço do Silêncio", "Alcançar o final em que você vende a própria alma.",
                "Achievement: Price of Silence", "Reach the ending where you sell your own soul.",
                "Logro: El Precio del Silencio", "Alcanzar el final en que vendes tu propia alma.",
                (p, id, fim, g) -> fim && id == FINAL_CUMPLICE));
        CATALOGO.add(new Conquista(
                "Conquista: Game Over", "Ser expulso da universidade.",
                "Achievement: Game Over", "Get expelled from the university.",
                "Logro: Game Over", "Ser expulsado de la universidad.",
                (p, id, fim, g) -> fim && id == FINAL_EXPULSAO));

        // --- Desafios dentro dos finais ---
        CATALOGO.add(new Conquista(
                "Conquista: Mente Fria", "Terminar a história com Estresse zerado.",
                "Achievement: Cold Mind", "Finish the story with zero Stress.",
                "Logro: Mente Fría", "Terminar la historia con Estrés en cero.",
                (p, id, fim, g) -> fim && p.getEstresse() == 0));
        CATALOGO.add(new Conquista(
                "Conquista: Nome Limpo", "Terminar o final do Detetive com Reputação 2 ou mais.",
                "Achievement: Clean Name", "Finish the Detective ending with Reputation 2 or higher.",
                "Logro: Nombre Limpio", "Terminar el final del Detective con Reputación 2 o más.",
                (p, id, fim, g) -> fim && id == FINAL_DETETIVE && p.getReputacao() >= 2));
        CATALOGO.add(new Conquista(
                "Conquista: Caso Sem Falhas", "Terminar o final do Detetive com todas as 5 pistas.",
                "Achievement: Flawless Case", "Finish the Detective ending with all 5 clues.",
                "Logro: Caso Sin Fallos", "Terminar el final del Detective con las 5 pistas.",
                (p, id, fim, g) -> fim && id == FINAL_DETETIVE && p.getPistas() >= 5));

        // --- Meta-conquistas ---
        CATALOGO.add(new Conquista(
                VETERANO, "Descobrir os 4 finais do jogo.",
                "Achievement: Investigation Veteran", "Discover all 4 endings of the game.",
                "Logro: Veterano de la Investigación", "Descubrir los 4 finales del juego.",
                (p, id, fim, g) -> g.getFinais().size() >= 4));
        CATALOGO.add(new Conquista(
                MESTRE, "Desbloquear todas as outras conquistas.",
                "Achievement: Master of Investigation", "Unlock all other achievements.",
                "Logro: Maestro de la Investigación", "Desbloquear todos los demás logros.",
                (p, id, fim, g) -> {
                    for (Conquista c : CATALOGO) {
                        if (c.getTitulo().equals(MESTRE)) continue;
                        if (!g.getDesbloqueaveis().contains(c.getTitulo())) return false;
                    }
                    return true;
                }));
    }

    private Conquistas() {}

    private static int relacao(Protagonista p, String npc) {
        return p.getRelacionamentos().getOrDefault(npc, 0);
    }

    /**
     * Devolve a lista (somente leitura) de todas as conquistas do jogo.
     */
    public static List<Conquista> todas() {
        return Collections.unmodifiableList(CATALOGO);
    }

    /**
     * Avalia todas as conquistas ainda não desbloqueadas e registra as que foram atingidas.
     * A verificação é repetida até estabilizar, para que a meta-conquista seja liberada
     * na mesma chamada em que a última conquista restante for obtida.
     *
     * @param p         Protagonista atual.
     * @param idCena    ID da cena em exibição.
     * @param cenaFinal Indica se a cena é um final.
     * @param global    Dados globais, onde as conquistas ficam registradas.
     * @return Lista das conquistas desbloqueadas agora (vazia se nenhuma).
     */
    public static List<Conquista> verificar(Protagonista p, int idCena, boolean cenaFinal, ProgressoJogo global) {
        List<Conquista> novas = new ArrayList<>();
        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (Conquista c : CATALOGO) {
                if (global.getDesbloqueaveis().contains(c.getTitulo())) continue;
                if (c.foiAtingida(p, idCena, cenaFinal, global) && global.adicionarDesbloqueavel(c.getTitulo())) {
                    novas.add(c);
                    mudou = true;
                }
            }
        }
        return novas;
    }
}
