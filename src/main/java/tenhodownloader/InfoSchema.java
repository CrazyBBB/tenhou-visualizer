package tenhodownloader;

import java.time.LocalDateTime;

public class InfoSchema {
    final public String id;
    final public String ma;
    final public String sou;
    final public String first;
    final public String second;
    final public String third;
    final public String fourth;
    final public LocalDateTime dateTime;

    public InfoSchema(String id, String ma, String sou, String first, String second, String third, String fourth, LocalDateTime dateTime) {
        this.id = id;
        this.ma = ma;
        this.sou = sou;
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return ma + "鳳" + sou + " " + first + ", " + second + ", " + third + ("".equals(fourth) ? "" : ", " + fourth);
    }

    public String getId() {
        return id;
    }
}
