package lol.hexbench;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;


public class HexBenchApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        AppContext appContext = new AppContext();

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

        double width = visualBounds.getWidth() * .65;
        double height = visualBounds.getHeight() * .65;

        FXMLLoader fxmlLoader = new FXMLLoader(HexBenchApplication.class.getResource("main-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), width, height);
        MainController controller = fxmlLoader.getController();
        controller.setAppContext(appContext);

        scene.getStylesheets().add(
                HexBenchApplication.class.getResource("style.css").toExternalForm()
        );

        stage.setTitle("HexBench");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
