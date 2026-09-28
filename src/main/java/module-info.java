module lol.hexbench {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;


    opens lol.hexbench to javafx.fxml;
    exports lol.hexbench;
    exports lol.hexbench.model;
    opens lol.hexbench.model to javafx.fxml;
    exports lol.hexbench.data;
    opens lol.hexbench.data to javafx.fxml;
}