package tenhouvisualizer;

public class MjlogFile {
    private byte[] xml;
    private int position;

    public MjlogFile(byte[] xml, int position) {
        this.xml = xml;
        this.position = position;
    }

    public byte[] getXml() {
        return xml;
    }

    public int getPosition() {
        return position;
    }

}
