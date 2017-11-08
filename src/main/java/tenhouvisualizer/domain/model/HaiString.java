package tenhouvisualizer.domain.model;

public class HaiString {
    private static final String[] haiStringArray = {
            "1m", "2m", "3m", "4m", "5m", "6m", "7m", "8m", "9m",
            "1p", "2p", "3p", "4p", "5p", "6p", "7p", "8p", "9p",
            "1s", "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s",
            "東", "南", "西", "北", "白", "發", "中"
    };
//    private static final String[] haiStringArray = {
//            "\uD83C\uDC07", "\uD83C\uDC08", "\uD83C\uDC09", "\uD83C\uDC0A", "\uD83C\uDC0B",
//            "\uD83C\uDC0C", "\uD83C\uDC0D", "\uD83C\uDC0E", "\uD83C\uDC0F",
//            "\uD83C\uDC19", "\uD83C\uDC1A", "\uD83C\uDC1B", "\uD83C\uDC1C", "\uD83C\uDC1D",
//            "\uD83C\uDC1E", "\uD83C\uDC1F", "\uD83C\uDC20", "\uD83C\uDC21",
//            "\uD83C\uDC10", "\uD83C\uDC11", "\uD83C\uDC12", "\uD83C\uDC13", "\uD83C\uDC14",
//            "\uD83C\uDC15", "\uD83C\uDC16", "\uD83C\uDC17", "\uD83C\uDC18",
//            "\uD83C\uDC00", "\uD83C\uDC01", "\uD83C\uDC02", "\uD83C\uDC03", "\uD83C\uDC06",
//            "\uD83C\uDC05", "\uD83C\uDC04"
//    };

    public static String getHaiStringByHaiId(int haiId) {
        return haiStringArray[haiId / 4];
    }

    public static String getHaiStringByIndex(int index) {
        return haiStringArray[index];
    }

    public static void main(String[] args) {
        for (String aHaiStringArray : haiStringArray) {
            System.out.println(aHaiStringArray);
        }
    }
}
