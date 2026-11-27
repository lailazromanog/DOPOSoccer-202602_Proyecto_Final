package domain;
import java.io.Serializable;

/**
 * Cronómetro de un partido jugado por tiempo.
 * Cuenta regresivamente desde un tiempo total dado y avisa cuándo se
 * acabó el tiempo reglamentario.
 */
public class Timer implements Serializable{
    private int totalTime;
    private double  currentTime;
    private boolean running;

    /**
     * Crea un cronómetro con un tiempo total por defecto de sesenta segundos.
     */
    public Timer() {
        this(60);

    }

    /**
     * Crea un cronómetro con el tiempo total dado, en segundos.
     * @param seconds tiempo total del cronómetro, en segundos
     */
    public Timer(int seconds){
        this.totalTime = seconds;
        this.currentTime = seconds;
    }

    /**
     * Pone en marcha el conteo del cronómetro.
     */
    public void start() {
        running = true;
    }

    /**
     * Detiene el conteo del cronómetro, sin reiniciar el tiempo restante.
     */
    public void stop() {
        running = false;
    }

    /**
     * Descuenta el tiempo transcurrido del tiempo restante, solo si el
     * cronómetro está en marcha, y nunca deja el tiempo restante por
     * debajo de cero.
     * @param deltaTime tiempo transcurrido desde la última actualización, en segundos
     */
    public void update(double deltaTime) {
        if (running) {
            currentTime -= deltaTime;
            if (currentTime < 0) {
                currentTime = 0;
            }
        }
    }

    /**
     * Indica si el tiempo restante ya se agotó.
     * @return verdadero si el tiempo restante llegó a cero, falso en caso contrario
     */
    public boolean isTimeUp() {
        return currentTime <= 0;
    }

    /**
     * Entrega el tiempo total con el que se creó el cronómetro, en segundos.
     * @return el tiempo total del cronómetro
     */
    public int getTotalTime() {
        return totalTime;
    }

    /**
     * Entrega el tiempo restante del cronómetro, redondeado hacia
     * arriba al segundo entero más cercano, para mostrarlo en pantalla.
     * @return el tiempo restante en segundos
     */
    public int getCurrentTime() {
        return (int) Math.ceil(currentTime);
    }

    /**
     * Indica si el cronómetro está en marcha en este momento.
     * @return verdadero si el cronómetro está en marcha, falso en caso contrario
     */
    public boolean isRunning() {
        return running;
    }
}
