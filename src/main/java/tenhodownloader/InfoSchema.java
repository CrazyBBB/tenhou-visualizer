package tenhodownloader;

import java.time.LocalDateTime;

public class InfoSchema {
    public final LocalDateTime dateTime;
    public final String taku;
    public final String id;
    public final int time;
    public final String payers;

    public InfoSchema(LocalDateTime dateTime, int time, String taku, String id, String payers) {
        this.dateTime = dateTime;
        this.taku = taku;
        this.id = id;
        this.time = time;
        this.payers = payers;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
