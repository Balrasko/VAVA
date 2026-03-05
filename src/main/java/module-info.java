module dev.vavateam1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens dev.vavateam1.controller to javafx.fxml;

    exports dev.vavateam1;
}
