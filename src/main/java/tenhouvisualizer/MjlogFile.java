package tenhouvisualizer;

public class MjlogFile {
    byte[] xml;
    Utils.Position position;

    public MjlogFile(byte[] xml, Utils.Position position) {
        this.xml = xml;
        this.position = position;
    }

    public byte[] getXml() {
        return xml;
    }

    public Utils.Position getPosition() {
        return position;
    }

}
