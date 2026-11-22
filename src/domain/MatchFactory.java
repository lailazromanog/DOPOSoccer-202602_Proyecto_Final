package domain;

/**
 * Creador de partidos.
 *
 * Unico lugar que sabe que tipos de partido existen (por tiempo, por goles, ...).
 * Game le pide un Match segun el modo y no conoce las subclases concretas.
 */
public class MatchFactory {

    public Match create(String matchType, int value, Player leftPlayer, Player rightPlayer,
                        Ball ball, Goal leftGoal, Goal rightGoal, Score score) {
        if (matchType.equals("time")) {
            Timer timer = new Timer(value * 60);              // value = minutos
            return new Time(leftPlayer, rightPlayer, ball, leftGoal, rightGoal, timer, score);
        } else {
            Timer timer = new Timer(0);
            return new Goals(leftPlayer, rightPlayer, ball, leftGoal, rightGoal, timer, score, value); // value = goles
        }
    }
}