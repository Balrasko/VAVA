package dev.vavateam1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TempOrderController {
    
    @FXML
    private Label orderLabel;

    public void setTableNumber(int tableNumber) {
        orderLabel.setText("Order view for table " + tableNumber);
    }

}
