package domain;

public class BlueAura extends Power {
    
    public BlueAura() {
        super("Blue Aura", 40, 1.5);
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

        if(opponent == match.getLeftPlayer()){
            opponent.setPositionX(opponent.getPositionX() - 100);
        } else {
            opponent.setPositionX(opponent.getPositionX() + 100);
        }
    }
}
