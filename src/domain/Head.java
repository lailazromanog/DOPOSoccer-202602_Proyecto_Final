package domain;
import java.io.Serializable;

public class Head implements Serializable{
    private String color;
    private String type; //nombre base de la imagen del personaje
    
    public Head(String type) {
        this.type = type;
        this.color = "default";
    }

    public String getColor() {
        return color;
    }

    public String getType() {
        return type;
    }
}
