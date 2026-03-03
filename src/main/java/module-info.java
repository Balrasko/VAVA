module dev.vavateam1 {

    requires javafx.controls;
    requires javafx.fxml;

    requires atlantafx.base;

    opens dev.vavateam1.controller to javafx.fxml;
    exports dev.vavateam1;
}