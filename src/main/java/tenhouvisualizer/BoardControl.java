package tenhouvisualizer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class BoardControl extends Canvas {
    private GraphicsContext gc;

    private Image[] img_nt = new Image[37];
    private Image[] img_ny = new Image[37];
    private Image[] img_dt = new Image[37];
    private Image[] img_dy = new Image[37];
    private Image imgUra;

    public void drawScene() {
        initBase();
    }

    public void drawScene(Scene scene) {
        initBase();
        initInfoAndHai(scene);
    }

    public BoardControl() {
        for (int i = 0; i < 37; i++) {
            Image[] images = Utils.manipulateHaiImage(BoardControl.class.getResourceAsStream("/img_s/" + i + ".png"));
            img_nt[i] = images[0];
            img_dt[i] = images[1];
            img_ny[i] = images[2];
            img_dy[i] = images[3];
        }
        imgUra = new Image("/img_s/ura.png");

        gc = this.getGraphicsContext2D();
    }

    private void initBase() {
        gc.clearRect(0, 0, this.getWidth(), this.getHeight());
        gc.setFill(javafx.scene.paint.Color.rgb(50, 100, 50));
        gc.fillRect(0, 0, this.getWidth(), this.getHeight());
        gc.setFill(javafx.scene.paint.Color.rgb(60, 63, 65));
        gc.fillRect(200, 200, 200, 200);
    }

    private void initInfoAndHai(Scene scene) {
        for (int i = 0; i < 4; i++) {
            int drawnPlayerId = (i + scene.heroPosition) % 4;

            if (drawnPlayerId != 3 || !scene.isSanma) {
                gc.setFill(Color.valueOf("#CD5F12"));
                gc.setFont(javafx.scene.text.Font.font(15));
                gc.fillText(scene.getZikaze(drawnPlayerId) + " " + String.valueOf(scene.playerPoints[drawnPlayerId]), 200, 368);

                gc.setFill(javafx.scene.paint.Color.valueOf("#bbbbbb"));
                gc.fillText(scene.playerDans[drawnPlayerId] + "R" + scene.playerRates[drawnPlayerId], 200, 383);

                gc.setFont(javafx.scene.text.Font.font(15));
                gc.fillText(scene.playerNames[drawnPlayerId], 200, 398);
                draw(scene, drawnPlayerId);
            }
            rotate();
        }

        gc.setFill(javafx.scene.paint.Color.valueOf("#bbbbbb"));
        gc.setFont(javafx.scene.text.Font.font(24));
        gc.fillText(scene.getBaStr(), 240, 290);

        gc.drawImage(imgUra, 240, 300, 20, 29);
        for (int i = 0; i < 4; i++) {
            if (i < scene.dora.size()) {
                gc.drawImage(getImage(scene.dora.get(i), true, true), 260 + 20 * i, 300, 20, 29);
            } else {
                gc.drawImage(imgUra, 260 + 20 * i, 300, 20, 29);
            }
        }
        gc.drawImage(imgUra, 340, 300, 20, 29);
    }

    private void draw(Scene scene, int playerId) {
        drawTehai(scene, playerId);
        drawDahai(scene, playerId);
        drawNaki(scene, playerId);
    }

    private void drawTehai(Scene scene, int playerId) {
        int x = 70;
        int y = 555;
        for (int hai : scene.tehaiSets.get(playerId)) {
            gc.drawImage(getImage(hai, true, true), x, y, 32, 45);
            x += 32;
        }

        if (scene.tsumo[playerId] != -1) {
            gc.drawImage(getImage(scene.tsumo[playerId], true, true), x + 4, y, 32, 45);
        }
    }

    private void drawDahai(Scene scene, int playerId) {
        int x = 200;
        int y = 400;
        int i = 0;
        for (int hai : scene.stehaiLists.get(playerId)) {
            if (i == scene.reach[playerId]) {
                gc.drawImage(getImage(hai, scene.tedashiLists.get(playerId).get(i), false), x, y + 13, 45, 32);
                x += 45;
            } else {
                gc.drawImage(getImage(hai, scene.tedashiLists.get(playerId).get(i), true), x, y, 32, 45);
                x += 32;
            }


            if (i == 5 || i == 11) {
                x = 200;
                y += 45;
            }

            i++;
        }

        if (scene.da[playerId] != -1) {
            gc.drawImage(getImage(scene.da[playerId], scene.daTedasi, true), x + 4, y + 4, 32, 45);
        }
    }

    private void drawNaki(Scene scene, int playerId) {
        int x = 600;
        int y = 555;

        int nOfKita = scene.kita[playerId];
        if (nOfKita > 0) {
            x -= 32;

            gc.drawImage(img_nt[30], x, y, 32, 45);
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font(15));
            gc.fillText("×" + nOfKita, 577, 553);
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
                        x -= 45;
                        gc.drawImage(getImage(naki.hai[i], true, false), x, y + 13, 45, 32);
                    } else {
                        x -= 32;
                        gc.drawImage(getImage(naki.hai[i], true, true), x, y, 32, 45);
                    }
                }
            } else if (naki.type == 2) {
                for (int i = 3; i >= 0; i--) {
                    x -= 32;
                    if (i == 0 || i == 3) {
                        gc.drawImage(imgUra, x, y, 32, 45);
                    } else {
                        gc.drawImage(getImage(naki.hai[i], true, true), x, y, 32, 45);
                    }
                }
            } else if (naki.type == 4) {
                for (int i = 2; i >= 0; i--) {
                    if (i == naki.nakiIdx) {
                        x -= 45;
                        gc.drawImage(getImage(naki.hai[i], true, false), x, y + 13, 45, 32);
                        gc.drawImage(getImage(naki.hai[3], true, false), x, y - 19, 45, 32);
                    } else {
                        x -= 32;
                        gc.drawImage(getImage(naki.hai[i], true, true), x, y, 32, 45);
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
        gc.translate(-600, 0);
    }

}
