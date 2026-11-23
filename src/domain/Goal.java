package domain;
import java.io.Serializable;

public class Goal implements Serializable{
    private double positionX;
    private double positionY;
    private int width;
    private int height;

    public Goal(double positionX, double positionY) {
       this(positionX, positionY, 10, 100); 
    }

    //permite definir el ancho y alto del arco 
    public Goal(double positionX, double positionY, int width, int height){
        this.positionX = positionX;
        this.positionY = positionY;
        this.width = width;
        this.height = height;
    }
    
    public boolean isGoal(Ball ball) {

        return ball.getPositionX() >= positionX && ball.getPositionX() <= positionX + width &&
               ball.getPositionY() >= positionY && ball.getPositionY() <= positionY + height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }
}
