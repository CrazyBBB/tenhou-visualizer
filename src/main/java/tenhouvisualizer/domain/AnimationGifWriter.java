package tenhouvisualizer.domain;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class AnimationGifWriter implements Closeable {
    private final BufferedImage bufferedImage;
    private final ImageWriter imageWriter;
    private final IIOMetadata metadata;
    public AnimationGifWriter(File file, int width, int height) throws IOException {
        Iterator it = ImageIO.getImageWritersByFormatName("gif");
        imageWriter = it.hasNext() ? (ImageWriter) it.next() : null;
        if (imageWriter == null) {
            throw new RuntimeException("ImageWriter for gif was not found");
        }

        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        metadata = imageWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bufferedImage), null);
        String format = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);
        int count = 0;
        byte[] data = {
                0x01,
                (byte) ((count >> 0) & 0xFF),
                (byte) ((count >> 8) & 0xFF)
        };
        IIOMetadataNode list = new IIOMetadataNode("ApplicationExtensions");
        IIOMetadataNode node = new IIOMetadataNode("ApplicationExtension");
        node.setAttribute("applicationID", "NETSCAPE");
        node.setAttribute("authenticationCode", "2.0");
        node.setUserObject(data);
        list.appendChild(node);
        root.appendChild(list);
        node = new IIOMetadataNode("GraphicControlExtension");
        node.setAttribute("disposalMethod", "none");
        node.setAttribute("userInputFlag", "FALSE");
        node.setAttribute("transparentColorFlag", "FALSE");
        node.setAttribute("delayTime", "15");
        node.setAttribute("transparentColorIndex", "0");
        root.appendChild(node);
        metadata.setFromTree(format, root);

        ImageOutputStream ios = ImageIO.createImageOutputStream(file);
        imageWriter.setOutput(ios);
        imageWriter.prepareWriteSequence(metadata);
    }

    public void writeImage(BufferedImage image) throws IOException {
        this.imageWriter.writeToSequence(new IIOImage(image, null, metadata), null);
    }

    public void close() throws IOException {
        this.imageWriter.endWriteSequence();
    }
}
