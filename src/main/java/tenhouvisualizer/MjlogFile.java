package tenhouvisualizer;

public class MjlogFile {
    byte[] xml;
    Utils.KAZE position;

    public MjlogFile(byte[] xml, Utils.KAZE position) {
        this.xml = xml;
        this.position = position;
    }

    public byte[] getXml() {
        return xml;
    }

    public Utils.KAZE getPosition() {
        return position;
    }

}
