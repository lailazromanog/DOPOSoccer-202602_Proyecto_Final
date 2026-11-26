package domain;

public class Tornado extends Power {
    public Tornado() {
        super("Tornado", 40, 3);
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

        opponent.launchUp(15);   //el tornado lo levanta del piso
        opponent.applyEffect("unconscious", getEffectDuration());
    }
}
