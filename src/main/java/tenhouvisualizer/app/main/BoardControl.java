package tenhouvisualizer.app.main;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenhouvisualizer.Main;
import tenhouvisualizer.domain.model.Naki;
import tenhouvisualizer.domain.model.MahjongScene;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BoardControl extends Canvas {

    private final static Logger log = LoggerFactory.getLogger(BoardControl.class);

    private GraphicsContext gc;

    private double baseSize;
    private double ratio;
    private double haiWidth;
    private double haiHeight;
    private double wanpaiWidth;
    private double wanpaiHeight;
    private double fontLSize;
    private double fontSSize;

    private Image[] img_nt = new Image[37];
    private Image[] img_ny = new Image[37];
    private Image[] img_dt = new Image[37];
    private Image[] img_dy = new Image[37];
    private Image imgUra;

    private MahjongScene currentScene = null;
    private int numberOfRotation = 0;

    Image backgroundImage;

    private static Image[] manipulateHaiImage(InputStream is) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int w = image.getWidth(null);
        int h = image.getHeight(null);

        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);

        BufferedImage[] bufferedImages = new BufferedImage[4];
        bufferedImages[0] = bufferedImage;

        int[] dpixels = bufferedImage.getRGB(0, 0, w, h, null, 0, w);
        int[] ypixels = new int[w * h];
        int[] dypixels = new int[w * h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int argb  = dpixels[w * y + x];
                int alpha = argb >> 24 & 0xFF;
                int red   = argb >> 16 & 0xFF;
                int green = argb >>  8 & 0xFF;
                int blue  = argb       & 0xFF;

                ypixels[h * (w - 1 - x) + y] = argb;

                red /= 2;
                green /= 2;
                blue /= 2;

                argb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                dpixels[w * y + x] = argb;
                dypixels[h * (w - 1 - x) + y] = argb;
            }
        }

        bufferedImages[1] = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        bufferedImages[1].setRGB(0, 0, w, h, dpixels, 0, w);

        bufferedImages[2] = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);
        bufferedImages[2].setRGB(0, 0, h, w, ypixels, 0, h);

        bufferedImages[3] = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);
        bufferedImages[3].setRGB(0, 0, h, w, dypixels, 0, h);

        WritableImage[] images = new WritableImage[4];
        for (int i = 0; i < 4; i++) {
            images[i] = SwingFXUtils.toFXImage(bufferedImages[i], null);
        }

        return images;
    }

    public void drawScene() {
        init();
    }

    public void drawScene(MahjongScene scene) {
        this.currentScene = scene;
        init();
        initInfoAndHai(scene);
    }

    public void redrawScene() {
        init();
        if (this.currentScene != null) {
            initInfoAndHai(this.currentScene);
        }
    }

    public void initViewpoint() {
        this.numberOfRotation = 0;
    }

    public void moveViewpointLeft() {
        this.numberOfRotation += 3;
        redrawScene();
    }

    public void moveViewpointRight() {
        this.numberOfRotation += 1;
        redrawScene();
    }

    public BoardControl() {
        if (Main.properties != null && Main.properties.containsKey("background")) {
            String backgroundFileName = Main.properties.getProperty("background");
            backgroundImage = new Image(new File(backgroundFileName).toURI().toString());
        }

        for (int i = 0; i < 37; i++) {
            Image[] images = manipulateHaiImage(BoardControl.class.getResourceAsStream("/img_s/" + i + ".png"));
            img_nt[i] = images[0];
            img_dt[i] = images[1];
            img_ny[i] = images[2];
            img_dy[i] = images[3];
        }
        imgUra = new Image("/img_s/ura.png");

        gc = this.getGraphicsContext2D();
    }

    private void init() {
        baseSize = Math.min(this.getWidth(), this.getHeight());
        ratio = baseSize / 600;
        haiWidth = 32 * ratio;
        haiHeight = 45 * ratio;
        wanpaiWidth = 20 * ratio;
        wanpaiHeight = 29 * ratio;
        fontLSize = 24 * ratio;
        fontSSize = 15 * ratio;

        gc.clearRect(0, 0, this.getWidth(), this.getHeight());
        gc.setFill(javafx.scene.paint.Color.rgb(50, 100, 50));
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, baseSize, baseSize);
        } else {
            gc.fillRect(0, 0, baseSize, baseSize);
        }
        gc.setFill(javafx.scene.paint.Color.rgb(60, 63, 65));
        gc.fillRect(200 * ratio, 200 * ratio, 200 * ratio, 200 * ratio);
    }

    private void initInfoAndHai(MahjongScene scene) {
        for (int i = 0; i < 4; i++) {
            int drawnPlayerId = (i + scene.heroPosition + numberOfRotation) % 4;

            if (drawnPlayerId != 3 || !scene.isSanma) {
                gc.setFill(Color.valueOf("#CD5F12"));
                gc.setFont(javafx.scene.text.Font.font(fontSSize));
                gc.fillText(scene.getZikaze(drawnPlayerId) + " " + String.valueOf(scene.playerPoints[drawnPlayerId]), 200 * ratio, 368 * ratio);

                gc.setFill(javafx.scene.paint.Color.valueOf("#bbbbbb"));
                gc.fillText(scene.playerDans[drawnPlayerId] + "R" + scene.playerRates[drawnPlayerId], 200 * ratio, 383 * ratio);

                gc.setFont(javafx.scene.text.Font.font(fontSSize));
                gc.fillText(scene.playerNames[drawnPlayerId], 200 * ratio, 398 * ratio);
                draw(scene, drawnPlayerId);
            }
            rotate();
        }

        gc.setFill(javafx.scene.paint.Color.valueOf("#bbbbbb"));
        gc.setFont(javafx.scene.text.Font.font(fontLSize));
        gc.fillText(scene.getBaStr(), 240 * ratio, 290 * ratio);
        drawTenbou(scene);

        gc.drawImage(imgUra, 240 * ratio, 300 * ratio, wanpaiWidth, wanpaiHeight);
        for (int i = 0; i < 4; i++) {
            if (i < scene.dora.size()) {
                gc.drawImage(getImage(scene.dora.get(i), true, true), 260 * ratio + wanpaiWidth * i, 300 * ratio, wanpaiWidth, wanpaiHeight);
            } else {
                gc.drawImage(imgUra, 260 * ratio + wanpaiWidth * i, 300 * ratio, wanpaiWidth, wanpaiHeight);
            }
        }
        gc.drawImage(imgUra, 340 * ratio, 300 * ratio, wanpaiWidth, wanpaiHeight);

        gc.setFill(Color.valueOf("#bbbbbb"));
        gc.setFont(javafx.scene.text.Font.font(14 * ratio));
        gc.fillText("残り " + scene.nokori, 280 * ratio, 345 * ratio);
    }

    private void drawTenbou(MahjongScene scene) {
        gc.setFill(Color.valueOf("#dddddd"));
        gc.fillRect(315 * ratio, 275 * ratio, 35 * ratio, 5 * ratio);
        gc.fillRect(315 * ratio, 285 * ratio, 35 * ratio, 5 * ratio);
        gc.setFill(Color.BLACK);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                gc.fillOval((330 + 2 * j) * ratio, (276 + 2 * i) * ratio, 1 * ratio, 1 * ratio);
            }
        }
        gc.setFill(Color.RED);
        gc.fillOval(332 * ratio, 286 * ratio, 3 * ratio, 3 * ratio);

        gc.setFill(Color.valueOf("#bbbbbb"));
        gc.setFont(javafx.scene.text.Font.font(9 * ratio));
        gc.fillText(String.valueOf(scene.honba), 355 * ratio, 280 * ratio);
        gc.fillText(String.valueOf(scene.kyotaku), 355 * ratio, 290 * ratio);
    }

    private void draw(MahjongScene scene, int playerId) {
        drawTehai(scene, playerId);
        drawDahai(scene, playerId);
        drawNaki(scene, playerId);
    }

    private void drawTehai(MahjongScene scene, int playerId) {
        double x = 70 * ratio;
        double y = 555 * ratio;
        for (int hai : scene.tehaiSets.get(playerId)) {
            if (scene.da[playerId] != hai) {
                gc.drawImage(getImage(hai, true, true), x, y, haiWidth, haiHeight);
            }
            x += haiWidth;
        }

        if (scene.tsumo[playerId] != -1 && scene.da[playerId] != scene.tsumo[playerId]) {
            gc.drawImage(getImage(scene.tsumo[playerId], true, true), x + 4 * ratio, y, haiWidth, haiHeight);
        }
    }

    private void drawDahai(MahjongScene scene, int playerId) {
        double x = 200 * ratio;
        double y = 400 * ratio;
        int i = 0;
        for (int hai : scene.stehaiLists.get(playerId)) {
            if (i == scene.reach[playerId]) {
                gc.drawImage(getImage(hai, scene.tedashiLists.get(playerId).get(i), false), x, y + (haiHeight - haiWidth), haiHeight, haiWidth);
                x += haiHeight;
            } else {
                gc.drawImage(getImage(hai, scene.tedashiLists.get(playerId).get(i), true), x, y, haiWidth, haiHeight);
                x += haiWidth;
            }


            if (i == 5 || i == 11) {
                x = 200 * ratio;
                y += haiHeight;
            }

            i++;
        }

        if (scene.da[playerId] != -1) {
            if (scene.daReach) {
                gc.drawImage(getImage(scene.da[playerId], scene.daTedasi, false), x + 4 * ratio, y + haiHeight - haiWidth + 4 * ratio, haiHeight, haiWidth);
            } else {
                gc.drawImage(getImage(scene.da[playerId], scene.daTedasi, true), x + 4 * ratio, y + 4 * ratio, haiWidth, haiHeight);
            }
        }
    }

    private void drawNaki(MahjongScene scene, int playerId) {
        double x = 600 * ratio;
        double y = 555 * ratio;

        int nOfKita = scene.kita[playerId];
        if (nOfKita > 0) {
            x -= haiWidth;

            gc.drawImage(img_nt[30], x, y, haiWidth, haiHeight);
            gc.setFill(Color.valueOf("#bbbbbb"));
            gc.setFont(javafx.scene.text.Font.font(fontSSize));
            gc.fillText("×" + nOfKita, 577 * ratio, 553 * ratio);
        }

        for (Naki naki : scene.naki.get(playerId)) {
            if (naki.type == 0 || naki.type == 1 || naki.type == 3) {
                int n;
                if (naki.type == 3) {
                    n = 3;
                } else {
                    n = 2;
                }
                for (int i = n; i >= 0; i--) {
                    if (i == naki.nakiIdx) {
                        x -= haiHeight;
                        gc.drawImage(getImage(naki.hai[i], true, false), x, y + haiHeight - haiWidth, haiHeight, haiWidth);
                    } else {
                        x -= haiWidth;
                        gc.drawImage(getImage(naki.hai[i], true, true), x, y, haiWidth, haiHeight);
                    }
                }
            } else if (naki.type == 2) {
                for (int i = 3; i >= 0; i--) {
                    x -= haiWidth;
                    if (i == 0 || i == 3) {
                        gc.drawImage(imgUra, x, y, haiWidth, haiHeight);
                    } else {
                        gc.drawImage(getImage(naki.hai[i], true, true), x, y, haiWidth, haiHeight);
                    }
                }
            } else if (naki.type == 4) {
                for (int i = 2; i >= 0; i--) {
                    if (i == naki.nakiIdx) {
                        x -= haiHeight;
                        gc.drawImage(getImage(naki.hai[i], true, false), x, y + haiHeight - haiWidth, haiHeight, haiWidth);
                        gc.drawImage(getImage(naki.hai[3], true, false), x, y + haiHeight - 2 * haiWidth, haiHeight, haiWidth);
                    } else {
                        x -= haiWidth;
                        gc.drawImage(getImage(naki.hai[i], true, true), x, y, haiWidth, haiHeight);
                    }
                }
            } else {
                throw new RuntimeException();
            }
        }
    }

    private Image getImage(int hai, boolean normal, boolean tate) {
        int haiId;
        if (hai == 16 || hai == 52 || hai == 88) {
            haiId = (hai - 16) / 36 + 34;
        } else {
            haiId = hai / 4;
        }

        if (normal) {
            if (tate) {
                return img_nt[haiId];
            } else {
                return img_ny[haiId];
            }
        } else {
            if (tate) {
                return img_dt[haiId];
            } else {
                return img_dy[haiId];
            }
        }
    }

    private void rotate() {
        gc.rotate(-90);
        gc.translate(-baseSize, 0);
    }

}
