module dev.vavateam1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens dev.vavateam1 to javafx.graphics;
    opens dev.vavateam1.controller to javafx.fxml;
}