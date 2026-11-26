package domain;

public class Stupid extends Bot {

    public Stupid() {
        super();
    }

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