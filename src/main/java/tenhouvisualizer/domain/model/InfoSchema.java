package tenhouvisualizer.domain.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InfoSchema {
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日H時m分");

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
        return ma + "鳳" + sou + " 1位:" + first + " 2位:" + second + " 3位:" + third
                + ("".equals(fourth) ? "" : " 4位:" + fourth) + " " + dateTime.format(dateFormatter);
    }

    public String getId() {
        return id;
    }
}
