package domain;
import java.io.Serializable;

public class Appereance implements Serializable{
    private Head head;
    private Shoes shoes;

    public Appereance(String type) {
        this.head = new Head(type);
        this.shoes = new Shoes(type);
    }

    public Head getHead() {
        return head;
    }

    public Shoes getShoes() {
        return shoes;
    }
}