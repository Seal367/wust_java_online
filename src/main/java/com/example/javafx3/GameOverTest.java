package com.example.javafx3;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.javafx3.manager.GameState;
import com.example.javafx3.manager.SkillManager;
import com.example.javafx3.ui.GameOverController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import java.net.URL;

/**
 * Test application to verify the game over screen functionality
 */
public class GameOverTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 50px; -fx-alignment: center;");
        
        Button testGameOverButton = new Button("Test Game Over Screen");
        testGameOverButton.setOnAction(e -> showTestGameOver(false));
        
        Button testWinButton = new Button("Test Win Screen");
        testWinButton.setOnAction(e -> showTestGameOver(true));
        
        Button testFallbackButton = new Button("Test Fallback Screen");
        testFallbackButton.setOnAction(e -> showFallbackGameOver(false));
        
        root.getChildren().addAll(testGameOverButton, testWinButton, testFallbackButton);
        
        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("Game Over Screen Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void showTestGameOver(boolean isWinScreen) {
        try {
            // Test resource loading
            URL fxmlResource = null;
            
            // Try multiple approaches
            fxmlResource = getClass().getResource("/fxml/game-over.fxml");
            if (fxmlResource == null) {
                fxmlResource = getClass().getClassLoader().getResource("fxml/game-over.fxml");
            }
            if (fxmlResource == null) {
                try {
                    fxmlResource = new java.io.File("resources/fxml/game-over.fxml").toURI().toURL();
                } catch (Exception ex) {
                    System.out.println("Could not load as file: " + ex.getMessage());
                }
            }
            
            if (fxmlResource == null) {
                System.out.println("FXML resource not found, using fallback");
                showFallbackGameOver(isWinScreen);
                return;
            }
            
            System.out.println("Loading FXML from: " + fxmlResource);
            
            FXMLLoader loader = new FXMLLoader(fxmlResource);
            StackPane gameOverRoot = loader.load();
            GameOverController controller = loader.getController();
            
            Stage gameOverStage = new Stage();
            gameOverStage.setTitle(isWinScreen ? "Victory!" : "Game Over");
            
            Scene gameOverScene = new Scene(gameOverRoot, 600, 500);
            
            // Try to load CSS
            URL cssResource = getClass().getResource("/css/game-over.css");
            if (cssResource != null) {
                gameOverScene.getStylesheets().add(cssResource.toExternalForm());
                System.out.println("CSS loaded successfully");
            } else {
                System.out.println("CSS not found, using default styling");
            }
            
            gameOverStage.setScene(gameOverScene);
            
            // Create test game state
            GameState testGameState = new GameState();
            testGameState.updateScore(350);
            SkillManager testSkillManager = new SkillManager(testGameState);
            
            controller.initializeController(
                testGameState,
                testSkillManager,
                isWinScreen,
                15, // max snake length
                35, // food consumed
                () -> {
                    System.out.println("Restart clicked!");
                    gameOverStage.close();
                },
                () -> {
                    System.out.println("Exit clicked!");
                    gameOverStage.close();
                }
            );
            
            gameOverStage.show();
            
        } catch (Exception e) {
            System.out.println("Error loading game over screen: " + e.getMessage());
            e.printStackTrace();
            showFallbackGameOver(isWinScreen);
        }
    }
    
    private void showFallbackGameOver(boolean isWinScreen) {
        System.out.println("Showing fallback game over screen");
        
        Stage fallbackStage = new Stage();
        fallbackStage.setTitle(isWinScreen ? "Victory!" : "Game Over");
        
        VBox container = new VBox(20);
        container.setStyle("-fx-padding: 40px; -fx-alignment: center; -fx-background-color: #2c3e50;");
        
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(isWinScreen ? "VICTORY!" : "GAME OVER");
        titleLabel.setStyle("-fx-text-fill: " + (isWinScreen ? "#2ecc71" : "#e74c3c") + 
                           "; -fx-font-size: 36px; -fx-font-weight: bold;");
        
        javafx.scene.control.Label scoreLabel = new javafx.scene.control.Label("Final Score: 350");
        scoreLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button restartBtn = new Button("🔄 PLAY AGAIN");
        restartBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                           "-fx-font-size: 16px; -fx-padding: 10px 20px;");
        restartBtn.setOnAction(e -> {
            System.out.println("Restart clicked!");
            fallbackStage.close();
        });
        
        Button exitBtn = new Button("🚪 EXIT GAME");
        exitBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-font-size: 16px; -fx-padding: 10px 20px;");
        exitBtn.setOnAction(e -> {
            System.out.println("Exit clicked!");
            fallbackStage.close();
        });
        
        container.getChildren().addAll(titleLabel, scoreLabel, restartBtn, exitBtn);
        
        Scene fallbackScene = new Scene(container, 400, 300);
        fallbackStage.setScene(fallbackScene);
        fallbackStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
