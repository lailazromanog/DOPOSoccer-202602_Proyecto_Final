package domain;

/**
 * Perfil de máquina Torpe.
 * Escoge sus movimientos, saltos y patadas al azar, sin seguir ninguna
 * estrategia frente al balón ni al rival.
 */
public class Stupid extends Bot {

    /**
     * Crea un bot con el perfil Torpe.
     */
    public Stupid() {
        super();
    }

    /**
     * Elige al azar si se mueve a la izquierda, a la derecha o se
     * queda quieto, y de vez en cuando también al azar decide saltar o
     * pedir una patada, sin tener en cuenta la posición del balón ni
     * el estado del partido.
     * @param match partido en curso sobre el que el bot toma su decisión
     */
    @Override
    public void decideAction(Match match) {
        // perfil Torpe: escoge sus movimientos al azar, sin estrategia
        int r = (int) (Math.random() * 3);
        if (r == 0) {
            moveLeft();
        } else if (r == 1) {
            moveRight();
        }
        // r == 2 -> se queda quieto ese frame

        // de vez en cuando salta o patea, tambien al azar
        if ((int) (Math.random() * 120) == 0) {
            jump();
        }
        if ((int) (Math.random() * 15) == 0) {
            wantsToKick = true;
        }
    }
}