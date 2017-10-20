package tenhouvisualizer.domain.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InfoSchema {
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日H時m分");

    final public String id;
    final public boolean isSanma;
    final public boolean isTonnan;
    final public int minute;
    final public LocalDateTime dateTime;
    final public String first;
    final public String second;
    final public String third;
    final public String fourth;
    final public int firstScore;
    final public int secondScore;
    final public int thirdScore;
    final public int fourthScore;

    public InfoSchema(String id, boolean isSanma, boolean isTonnan, int minute, LocalDateTime dateTime,
                      String first, String second, String third, String fourth,
                      int firstScore, int secondScore, int thirdScore, int fourthScore) {
        this.id = id;
        this.isSanma = isSanma;
        this.isTonnan = isTonnan;
        this.minute = minute;
        this.dateTime = dateTime;
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.firstScore = firstScore;
        this.secondScore = secondScore;
        this.thirdScore = thirdScore;
        this.fourthScore = fourthScore;
    }

    public InfoSchema(String id, boolean isSanma, boolean isTonnan, LocalDateTime dateTime,
                      String first, String second, String third, String fourth) {
        this(
                id,
                isSanma,
                isTonnan,
                0,
                dateTime,
                first,
                second,
                third,
                fourth,
                0,
                0,
                0,
                0
        );
    }

    @Override
    public String toString() {
        return (isSanma ? "三" : "四") + "鳳" + (isTonnan ? "南" : "東")
                + " 1位:" + first + " 2位:" + second + " 3位:" + third
                + (isSanma ? "" : " 4位:" + fourth) + " " + dateTime.format(dateFormatter);
    }

    public String getId() {
        return id;
    }
}
