package tenhouvisualizer.domain.model;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class InfoSchemaTest {
    @Test
    public void testToString() throws Exception {
        InfoSchema infoSchema = new InfoSchema.Builder(
                "001",
                false,
                false,
                LocalDateTime.of(
                        2017,
                        11,
                        1,
                        15,
                        28),
                "aaa",
                "bbb",
                "ccc",
                "ddd"
                ).build();
        String expected = "四鳳東 1位:aaa 2位:bbb 3位:ccc 4位:ddd 2017年11月1日15時28分";
        assertEquals(expected, infoSchema.toString());
    }

    @Test
    public void testGetMinute() throws Exception {
        InfoSchema infoSchema = new InfoSchema.Builder(
                "001",
                false,
                false,
                LocalDateTime.of(2017, 11, 1, 15, 28),
                "aaa",
                "bbb",
                "ccc",
                "ddd"
        ).minute(334).build();
        assertEquals(334, infoSchema.getMinute());
    }
}