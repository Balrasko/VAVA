package dev.vavateam1.controller;

import dev.vavateam1.model.Table;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TempOrderController {

    @FXML
    private Label orderLabel;

    public void setTable(Table table) {
        orderLabel.setText("Order view for table " + table.getTableNumber());
    }
}
