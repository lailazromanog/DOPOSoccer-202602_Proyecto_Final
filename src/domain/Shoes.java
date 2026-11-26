package domain;
import java.io.Serializable;

public class Shoes implements Serializable{
    private String color;
    private String type;   //mismo nombre base de la imagen que la cabeza

    public Shoes(String type) {
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