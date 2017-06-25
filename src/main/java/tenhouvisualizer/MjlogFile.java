package tenhouvisualizer;

public class MjlogFile {
    byte[] xml;
    int position;

    public MjlogFile(byte[] xml, int position) {
        this.xml = xml;
        this.position = position;
    }

    public void setXml(byte[] xml) {
        this.xml = xml;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public byte[] getXml() {
        return xml;
    }

    public int getPosition() {
        return position;
    }
}
