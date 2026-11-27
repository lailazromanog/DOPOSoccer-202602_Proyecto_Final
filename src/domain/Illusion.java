package domain;

import java.util.ArrayList;
import java.util.List;

public class Illusion extends Power {
    public Illusion() {
        super("Illusion", 30, 2.5);
    }

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