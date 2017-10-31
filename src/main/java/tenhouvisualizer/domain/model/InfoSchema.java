package tenhouvisualizer.domain.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InfoSchema {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日H時m分");

    private final String id;
    private final boolean isSanma;
    private final boolean isTonnan;
    private final LocalDateTime dateTime;
    private final String first;
    private final String second;
    private final String third;
    private final String fourth;
    private final int minute;
    private final int firstScore;
    private final int secondScore;
    private final int thirdScore;
    private final int fourthScore;

    public static class Builder {
        private final String id;
        private final boolean isSanma;
        private final boolean isTonnan;
        private final LocalDateTime dateTime;
        private final String first;
        private final String second;
        private final String third;
        private final String fourth;

        private int minute = 0;
        private int firstScore = 0;
        private int secondScore = 0;
        private int thirdScore = 0;
        private int fourthScore = 0;

        public Builder(String id, boolean isSanma, boolean isTonnan, LocalDateTime dateTime,
                       String first, String second, String third, String fourth) {
            this.id = id;
            this.isSanma = isSanma;
            this.isTonnan = isTonnan;
            this.dateTime = dateTime;
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
        }

        public Builder minute(int val) {
            minute = val;
            return this;
        }

        public Builder firstScore(int val) {
            firstScore = val;
            return this;
        }

        public Builder secondScore(int val) {
            secondScore = val;
            return this;
        }

        public Builder thirdScore(int val) {
            thirdScore = val;
            return this;
        }

        public Builder fourthScore(int val) {
            fourthScore = val;
            return this;
        }

        public InfoSchema build() {
            return new InfoSchema(this);
        }
    }

    private InfoSchema(Builder builder) {
        id = builder.id;
        isSanma = builder.isSanma;
        isTonnan = builder.isTonnan;
        minute = builder.minute;
        dateTime = builder.dateTime;
        first = builder.first;
        second = builder.second;
        third = builder.third;
        fourth = builder.fourth;
        firstScore = builder.firstScore;
        secondScore = builder.secondScore;
        thirdScore = builder.thirdScore;
        fourthScore = builder.fourthScore;
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

    public boolean isSanma() {
        return isSanma;
    }

    public boolean isTonnan() {
        return isTonnan;
    }

    public int getMinute() {
        return minute;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    public String getThird() {
        return third;
    }

    public String getFourth() {
        return fourth;
    }

    public int getFirstScore() {
        return firstScore;
    }

    public int getSecondScore() {
        return secondScore;
    }

    public int getThirdScore() {
        return thirdScore;
    }

    public int getFourthScore() {
        return fourthScore;
    }
}
