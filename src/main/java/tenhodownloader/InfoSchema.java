package tenhodownloader;

import java.time.LocalDateTime;

public class InfoSchema {
    final String id;
    final String ma;
    final String sou;
    final String first;
    final String second;
    final String third;
    final String fourth;
    final LocalDateTime dateTime;

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
        return this.id;
    }
}
