package domain;

/**
 * Creador de partidos.
 *
 * Unico lugar que sabe que tipos de partido existen (por tiempo, por goles, ...).
 * Game le pide un Match segun el modo y no conoce las subclases concretas.
 */
public class MatchFactory {

    /**
     * Crea el partido correspondiente al tipo de partido elegido.
     * @param matchType tipo de partido a crear, por tiempo o por goles
     * @param value minutos de duración del partido si es por tiempo, o número de goles para ganar si es por goles
     * @param leftPlayer jugador que defiende el lado izquierdo de la cancha
     * @param rightPlayer jugador que defiende el lado derecho de la cancha
     * @param ball balón del partido
     * @param leftGoal arco izquierdo de la cancha
     * @param rightGoal arco derecho de la cancha
     * @param score marcador del partido
     * @return el partido creado con ese tipo
     * @throws DOPOSoccerException si el tipo de partido dado no corresponde a ningún tipo de partido disponible
     */
    public Match create(String matchType, int value, Player leftPlayer, Player rightPlayer,
                        Ball ball, Goal leftGoal, Goal rightGoal, Score score) throws DOPOSoccerException {
        if (matchType.equals("time")) {
            Timer timer = new Timer(value * 60);              // value = minutos
            return new Time(leftPlayer, rightPlayer, ball, leftGoal, rightGoal, timer, score);
        } else if (matchType.equals("goals")) {
            Timer timer = new Timer(0);
            return new Goals(leftPlayer, rightPlayer, ball, leftGoal, rightGoal, timer, score, value); // value = goles
        } else {
            throw new DOPOSoccerException("Tipo de partido no reconocido: " + matchType);
        }
    }
}