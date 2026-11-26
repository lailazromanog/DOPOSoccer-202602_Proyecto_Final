package domain;

public abstract class Bot extends Player {
    
    protected boolean wantsToKick;   // el bot pide patear; Game ejecuta la patada real

    public Bot() {
        super();
        wantsToKick = false;
    }

    public abstract void decideAction(Match match);

    public boolean wantsToKick() {
        return wantsToKick;
    }

    public void clearKickIntent() {
        wantsToKick = false;
    }
}