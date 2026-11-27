package domain;
import java.io.Serializable;

/**
 * Representa el arco al que un jugador debe meter el balón para anotar.
 * Define una zona rectangular de la cancha y determina si el balón
 * entró dentro de esa zona.
 */
public class Goal implements Serializable{
    private double positionX;
    private double positionY;
    private int width;
    private int height;

    /**
     * Crea un arco en la posición dada, con un ancho y un alto por defecto.
     * @param positionX posición horizontal del arco dentro de la cancha
     * @param positionY posición vertical del arco dentro de la cancha
     */
    public Goal(double positionX, double positionY) {
       this(positionX, positionY, 10, 100);
    }

    //permite definir el ancho y alto del arco
    /**
     * Crea un arco en la posición dada, con el ancho y el alto propios
     * de su zona de gol.
     * @param positionX posición horizontal del arco dentro de la cancha
     * @param positionY posición vertical del arco dentro de la cancha
     * @param width ancho de la zona de gol del arco
     * @param height alto de la zona de gol del arco
     */
    public Goal(double positionX, double positionY, int width, int height){
        this.positionX = positionX;
        this.positionY = positionY;
        this.width = width;
        this.height = height;
    }

    /**
     * Indica si el balón entró dentro de la zona de gol de este arco.
     * @param ball balón del que se revisa la posición
     * @return verdadero si el balón está dentro de la zona de gol, falso en caso contrario
     */
    public boolean isGoal(Ball ball) {

        return ball.getPositionX() >= positionX && ball.getPositionX() <= positionX + width &&
               ball.getPositionY() >= positionY && ball.getPositionY() <= positionY + height;
    }

    /**
     * Entrega el ancho de la zona de gol del arco.
     * @return el ancho de la zona de gol
     */
    public int getWidth() {
        return width;
    }

    /**
     * Entrega el alto de la zona de gol del arco.
     * @return el alto de la zona de gol
     */
    public int getHeight() {
        return height;
    }

    /**
     * Entrega la posición horizontal del arco dentro de la cancha.
     * @return la posición horizontal del arco
     */
    public double getPositionX() {
        return positionX;
    }

    /**
     * Entrega la posición vertical del arco dentro de la cancha.
     * @return la posición vertical del arco
     */
    public double getPositionY() {
        return positionY;
    }
}
