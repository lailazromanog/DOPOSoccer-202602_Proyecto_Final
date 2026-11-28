package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiro de poder del personaje de Estados Unidos.
 * El balón se divide en ocho balones idénticos, uno real y siete
 * señuelos, y si golpea al rival lo deja confundido sin saber cuál es
 * el balón real mientras dura el efecto.
 */
public class Illusion extends Power {

    //nombre del efecto que queda sobre el rival al ser golpeado
    private static final String EFFECT = "confused";

    /**
     * Crea el tiro de poder Ilusión con su daño base y la duración del
     * efecto de confusión que deja sobre el rival.
     */
    public Illusion() {
        super("Illusion", 30, 2.5);
    }

    /**
     * Divide el balón en ocho balones idénticos, el real y siete
     * señuelos que salen desde la misma posición en direcciones al azar,
     * y entrega los señuelos al partido para que los mueva y los quite
     * al terminar la duración propia de este tiro de poder.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    @Override
    public void shoot(Match match, Player attacker) {

        // efecto Illusion: el balon se divide en 8 (1 real + 7 senuelos identicos).
        // El poder CREA sus propios senuelos; Match solo los guarda y los actualiza.
        Ball real = match.getBall();
        List<Ball> fakes = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Ball fake = new Ball();
            fake.setPositionX(real.getPositionX());
            fake.setPositionY(real.getPositionY());
            double angle = Math.random() * 2 * Math.PI;
            double speed = 9 + Math.random() * 6;
            fake.setVelocityX(Math.cos(angle) * speed);
            fake.setVelocityY(Math.sin(angle) * speed);
            fakes.add(fake);
        }
        match.addFakeBalls(fakes, getEffectDuration());
    }

    /**
     * Deja al rival confundido durante la duración del efecto, sin
     * saber cuál de los balones en la cancha es el real.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que ejecutó el tiro de poder
     * @param opponent jugador rival que fue golpeado por el tiro de poder
     */
    @Override
    protected void applyHitEffect(Match match, Player attacker, Player opponent) {
        //el rival queda confundido mientras dura el efecto
        opponent.applyEffect(EFFECT, getEffectDuration());
    }
}