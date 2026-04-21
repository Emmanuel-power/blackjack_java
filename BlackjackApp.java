package com.blackjackgame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point.
 * Launches the window and hands everything to GameController.
 *
 * To run:  mvn javafx:run
 */
public class BlackjackApp extends Application {

    private static final int WINDOW_WIDTH  = 920;
    private static final int WINDOW_HEIGHT = 660;

    @Override
    public void start(Stage stage) {
        GameController controller = new GameController();

        Scene scene = new Scene(controller.getRoot(), WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle("Blackjack");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
