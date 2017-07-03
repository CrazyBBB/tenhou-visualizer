package tenhouvisualizer;

public class MjlogFile {
    byte[] xml;
    Position position;

    public MjlogFile(byte[] xml, Position position) {
        this.xml = xml;
        this.position = position;
    }

    public byte[] getXml() {
        return xml;
    }

    public Position getPosition() {
        return position;
    }

    public enum Position {
        TON,
        NAN,
        SHA,
        PE
    }
}
