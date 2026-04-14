module dev.vavateam1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.google.guice;
    requires java.sql;
    requires org.postgresql.jdbc;

    requires static lombok;
    requires javafx.graphics;

    opens dev.vavateam1 to javafx.graphics, javafx.fxml, com.google.guice;
    opens dev.vavateam1.controller to javafx.fxml, com.google.guice;
    opens dev.vavateam1.model to javafx.base, com.google.guice;
    opens dev.vavateam1.dao to com.google.guice;
    opens dev.vavateam1.service to com.google.guice;
    opens dev.vavateam1.data.connection to com.google.guice;
    opens dev.vavateam1.data.config to com.google.guice;
    opens dev.vavateam1.data.initializer to com.google.guice;
    opens dev.vavateam1.util to com.google.guice;

    exports dev.vavateam1;
    opens dev.vavateam1.dto to com.google.guice, javafx.base;
    opens dev.vavateam1.report to com.google.guice, javafx.base;
}
