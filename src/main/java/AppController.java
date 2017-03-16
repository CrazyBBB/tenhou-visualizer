import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AppController implements Initializable {
    @FXML
    private BorderPane root;
    @FXML
    private Label label;
    @FXML
    private ListView<Scene> listview;
    @FXML
    private Canvas canvas;

    private File selectedFile;
    private GraphicsContext gc;

    private Image[] img_nt = new Image[37];
    private Image[] img_ny = new Image[37];
    private Image[] img_dt = new Image[37];
    private Image[] img_dy = new Image[37];

    @FXML
    public void onBtnClicked(ActionEvent e) throws IOException, ParserConfigurationException, SAXException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("."));
        selectedFile = fc.showOpenDialog(root.getScene().getWindow());

        if (selectedFile != null) {
            label.setText(selectedFile.toString());
            ArrayList<byte[]> list = Reader.unzip(selectedFile);
            for (byte[] xml : list) {
                Document document = Reader.convertXmlFileToDocument(Reader.gunzip(xml));
                ArrayList<Scene> scenes = Analyzer.findOriScenes(document);
                for (Scene scene : scenes) listview.getItems().add(scene);
            }
        }
    }

    private void draw(Scene scene, int playerId) {
        drawTehai(scene, playerId);
        drawDahai(scene, playerId);
        drawNaki(scene, playerId);
    }

    private void drawTehai(Scene scene, int playerId) {
        int x = 70;
        int y = 555;
        for (int hai : scene.stehai[playerId]) {
            gc.drawImage(getImage(hai, true, true), x, y);
            x += 32;
        }
    }

    private void drawDahai(Scene scene, int playerId) {
        int x = 200;
        int y = 400;
        int i = 0;
        for (int hai : scene.dahai[playerId]) {
            if (i == scene.reach[playerId]) {
                gc.drawImage(getImage(hai, scene.tedashi[playerId].get(i), false), x, y + 13);
                x += 45;
            } else {
                gc.drawImage(getImage(hai, scene.tedashi[playerId].get(i), true), x, y);
                x += 32;
            }


            if (i == 5 || i == 11) {
                x = 200;
                y += 45;
            }

            i++;
        }
    }

    private void drawNaki(Scene scene, int playerId) {
        int x = 600;
        int y = 555;

        int nOfKita = scene.kita[playerId];
        if (nOfKita > 0) {
            x -= 32;

            gc.drawImage(img_nt[30], x, y);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(15));
            gc.fillText("×" + nOfKita, 577, 555);
        }

        for (Naki naki : scene.naki[playerId]) {
            if (naki.type == 0 || naki.type == 1) {
                for (int i = 2; i >= 0; i--) {
                    if (i == naki.nakiIdx) {
                        x -= 45;
                        gc.drawImage(getImage(naki.hai[i], true, false), x, y + 13);
                    } else {
                        x -= 32;
                        gc.drawImage(getImage(naki.hai[i], true, true), x, y);
                    }
                }
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 0; i < 37; i++) {
            img_nt[i] = new Image("img_nt/" + i + ".png");
            img_ny[i] = new Image("img_ny/" + i + ".png");
            img_dt[i] = new Image("img_dt/" + i + ".png");
            img_dy[i] = new Image("img_dy/" + i + ".png");
        }

        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.valueOf("#cccccc"));
        gc.fillRect(200, 200, 200, 200);

        listview.getSelectionModel().selectedItemProperty().addListener((obs, oldScene, newScene) -> {
            init(newScene);
        });
    }

    private void init(Scene scene) {
        label.setText(scene.toString());

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.valueOf("#cccccc"));
        gc.fillRect(200, 200, 200, 200);

        gc.setFill(Color.BLACK);
        for (int i = 0; i < 4; i++) {
            if (i == 3 && scene.isSanma) {
                rotate();
                break;
            }
            gc.setFill(Color.RED);
            gc.setFont(Font.font(15));
            gc.fillText(scene.getZikaze(i) + " " + String.valueOf(scene.point[i]), 200, 370);

            gc.setFill(Color.BLACK);
            gc.fillText(scene.dan[i] + "R" + scene.rate[i], 200, 385);

            gc.setFont(Font.font("MS Mincho", 15));
            gc.fillText(scene.players[i], 200, 400);
            draw(scene, i);
            rotate();
        }
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }
}