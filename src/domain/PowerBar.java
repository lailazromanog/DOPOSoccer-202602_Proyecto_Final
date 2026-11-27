package domain;
import java.io.Serializable;

/**
 * Representa la barra de poder de un jugador.
 * Se llena sola con el paso del tiempo, en diez segundos, y aumenta
 * de inmediato un cuarto cuando el jugador recibe un gol, hasta
 * quedar llena y lista para activar el poder del jugador.
 */
public class PowerBar implements Serializable{
    private double currentCharge;
    private double maxCharge;

    /**
     * Crea una barra de poder vacía, con su carga máxima fijada en cien.
     */
    public PowerBar() {
        this.maxCharge = 100;
        this.currentCharge = 0;
    }

    /**
     * Carga la barra de poder según el tiempo transcurrido, de forma
     * que se llena por completo en diez segundos, sin pasarse nunca
     * de la carga máxima.
     * @param deltaTime tiempo transcurrido desde la última actualización, en segundos
     */
    public void update(double deltaTime) {
        currentCharge += (maxCharge / 10) * deltaTime;
        if (currentCharge > maxCharge) {
            currentCharge = maxCharge;
        }
    }

    /**
     * Carga de inmediato un cuarto de la barra de poder, para cuando
     * el jugador recibe un gol, sin pasarse nunca de la carga máxima.
     */
    public void chargeOnGoal(){
        currentCharge += maxCharge / 4;
        if (currentCharge > maxCharge) {
            currentCharge = maxCharge;
        }
    }

    /**
     * Vacía la barra de poder, para cuando el jugador activa su poder.
     */
    public void reset() {
        currentCharge = 0;
    }

    /**
     * Indica si la barra de poder está completamente llena.
     * @return verdadero si la barra está llena, falso en caso contrario
     */
    public boolean isFull() {
        return currentCharge >= maxCharge;
    }

    /**
     * Entrega la carga actual de la barra de poder.
     * @return la carga actual de la barra de poder
     */
    public double getCurrentCharge() {
        return currentCharge;
    }

    /**
     * Entrega la carga máxima de la barra de poder.
     * @return la carga máxima de la barra de poder
     */
    public double getMaxCharge() {
        return maxCharge;
    }
}
