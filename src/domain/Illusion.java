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
    /**
     * Crea el tiro de poder Ilusión con su daño base y la duración del
     * efecto de confusión que deja sobre el rival.
     */
    public Illusion() {
        super("Illusion", 30, 2.5);
    }

    /**
     * Aplica daño al rival del atacante, crea los balones señuelo
     * idénticos al balón real y los entrega al partido en curso para
     * que los actualice y los muestre durante la duración propia de
     * este tiro de poder.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    @Override
    public void activate(Match match, Player attacker) {
        Player opponent;
        if(attacker == match.getLeftPlayer()){
            opponent = match.getRightPlayer();
        } else {
            opponent = match.getLeftPlayer();
        }

        double damage = getDamageTiro() * attacker.getMultiplicadorKick();
        opponent.takeDamage(damage);

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
}