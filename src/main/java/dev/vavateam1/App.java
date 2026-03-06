package dev.vavateam1;

import com.google.inject.Guice;
import com.google.inject.Injector;

import dev.vavateam1.controller.ViewSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Injector injector = Guice.createInjector(new AppModule());
        injector.getInstance(ViewSwitcher.class).InitStage(stage, injector);
    }

    public static void main(String[] args) {
        launch();
    }
}
