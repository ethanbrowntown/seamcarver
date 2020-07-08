import edu.princeton.cs.algs4.Picture;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class GUI extends Application {
    Picture pic;
    SeamCarver sc;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Seam Carving");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        Scene scene = new Scene(grid, 600, 400);

        primaryStage.setScene(scene);
        primaryStage.show();

        Text scenetitle = new Text("Seam Carver");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label widthLabel = new Label("Width:");
        grid.add(widthLabel, 0, 1);

        TextField widthTextField = new TextField();
        grid.add(widthTextField, 1, 1);

        Label heightLabel = new Label("Height:");
        grid.add(heightLabel, 0, 2);

        TextField heightTextField = new TextField();
        grid.add(heightTextField, 1, 2);

        Button btn = new Button("Process");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);

        Button uploadBtn = new Button("Upload image");
        HBox hbUploadBtn = new HBox(10);
        hbUploadBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbUploadBtn.getChildren().add(uploadBtn);
        grid.add(hbUploadBtn, 0, 4);

        Button saveBtn = new Button("Save Image");
        HBox hbSaveBtn = new HBox(10);
        hbSaveBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbSaveBtn.getChildren().add(saveBtn);

        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);

        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Image");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    pic = new Picture(file);
                    actiontarget.setFill(Color.BLACK);
                    actiontarget.setText("Selected: " + file.getName());
                } catch (IllegalArgumentException i) {
                    i.printStackTrace();
                    actiontarget.setFill(Color.FIREBRICK);
                    actiontarget.setText("No valid image file found!");
                }
            }
        });

        btn.setOnAction(e -> {
            try {
                int width = Integer.parseInt(widthTextField.getText());
                int height = Integer.parseInt(heightTextField.getText());
                if (pic == null) {
                    actiontarget.setFill(Color.FIREBRICK);
                    actiontarget.setText("Please upload image!");
                } else if (width < 1 || height < 1) {
                    actiontarget.setFill(Color.FIREBRICK);
                    actiontarget.setText("Width and height must be at least 1!");
                } else if (width > pic.width() || height > pic.height()) {
                    actiontarget.setFill(Color.FIREBRICK);
                    actiontarget.setText("Width and height must not be larger than target image!");
                } else {
                    actiontarget.setFill(Color.BLACK);
                    sc = new SeamCarver(pic);
                    int oldWidth = sc.width();
                    int oldHeight = sc.height();
                    for (int i = 0; i < oldWidth - width; i++) {
                        sc.removeVerticalSeam(sc.findVerticalSeam());
                    }
                    for (int i = 0; i < oldHeight - height; i++) {
                        sc.removeHorizontalSeam(sc.findHorizontalSeam());
                    }
                    actiontarget.setText("Completed");
                    grid.add(hbSaveBtn, 0, 5);
                }
            } catch (NumberFormatException n) {
                n.printStackTrace();
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("Width and Height must be whole numbers!");
            }
        });

        saveBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png")
            );
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                sc.picture().save(file);
            }
            actiontarget.setText("Saved");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
