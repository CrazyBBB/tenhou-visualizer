import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
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

    private Image[] images = new Image[34];
    private Random random = new Random();

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

    private void draw() {
        int x, y;

        x = 200;
        y = 400;
        for (int i = 0; i < 15; i++) {
            gc.drawImage(images[getId()], x, y);

            if (i % 6 == 5) {
                x = 200;
                y += 45;
            } else {
                x += 32;
            }
        }

        x = 70;
        y = 555;
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            list.add(getId());
        }
        Collections.sort(list);
        for (int i = 0; i < 13; i++) {
            gc.drawImage(images[list.get(i)], x, y);
            x += 32;
        }
    }

    private int getId() {
        int ret = 1;
        while (ret >= 1 && ret <= 7) ret = random.nextInt(34);
        return ret;
    }

    private void rotate() {
        //gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        //gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.rotate(-90);
        gc.translate(-600, 0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 0; i < 34; i++) {
            images[i] = new Image("img/" + i + ".png");
        }

        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.valueOf("#cccccc"));
        gc.fillRect(200, 200, 200, 200);

        gc.setFont(Font.font(20));

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
        for (int i = 0; i < 3; i++) {
            gc.fillText(scene.dan[i] + " R" + scene.rate[i], 200, 380);
            gc.fillText(scene.players[i], 200, 400);
            draw();
            rotate();
        }
        rotate();
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }
}