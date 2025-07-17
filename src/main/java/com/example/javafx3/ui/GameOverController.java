package com.example.javafx3.ui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import com.example.javafx3.manager.GameState;
import com.example.javafx3.manager.SkillManager;
import com.example.javafx3.model.SkillCard;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 游戏结束界面控制器，包含全面的统计信息和动画效果
 */
public class GameOverController implements Initializable {
    
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label scoreLabel;
    @FXML private Label timePlayedLabel;
    @FXML private Label skillsEarnedLabel;
    @FXML private Label foodConsumedLabel;
    @FXML private Label maxLengthLabel;
    @FXML private Label skillsSummaryLabel;
    @FXML private Label bestSkillLabel;
    @FXML private Button restartButton;
    @FXML private Button exitButton;
    @FXML private HBox achievementContainer;
    @FXML private VBox skillsSummarySection;
    
    private GameState gameState;
    private SkillManager skillManager;
    private Runnable onRestartCallback;
    private Runnable onExitCallback;
    private boolean isWinScreen = false;
    private int maxSnakeLength = 3;
    private int foodConsumed = 0;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置键盘事件处理
        Platform.runLater(() -> {
            if (restartButton.getScene() != null) {
                restartButton.getScene().setOnKeyPressed(this::handleKeyPress);

                // 根据屏幕大小应用紧凑样式
                double sceneWidth = restartButton.getScene().getWidth();
                double sceneHeight = restartButton.getScene().getHeight();

                if (sceneWidth <= 600 || sceneHeight <= 500) {
                    restartButton.getScene().getRoot().getStyleClass().add("compact");
                }
            }
        });

        // 设置初始焦点
        Platform.runLater(() -> restartButton.requestFocus());
    }
    
    /**
     * 使用游戏数据和回调函数初始化控制器
     */
    public void initializeController(GameState gameState, SkillManager skillManager, 
                                   boolean isWinScreen, int maxSnakeLength, int foodConsumed,
                                   Runnable onRestartCallback, Runnable onExitCallback) {
        this.gameState = gameState;
        this.skillManager = skillManager;
        this.isWinScreen = isWinScreen;
        this.maxSnakeLength = maxSnakeLength;
        this.foodConsumed = foodConsumed;
        this.onRestartCallback = onRestartCallback;
        this.onExitCallback = onExitCallback;
        
        setupScreen();
        playEntryAnimations();
    }
    
    /**
     * 根据游戏状态设置界面内容
     */
    private void setupScreen() {
        // 根据胜负设置标题和副标题
        if (isWinScreen) {
            titleLabel.setText("胜利！");
            titleLabel.getStyleClass().remove("game-over-title");
            titleLabel.getStyleClass().add("game-win-title");
            subtitleLabel.setText("🎉 恭喜！您已经掌握了游戏！🎉");
        } else {
            titleLabel.setText("游戏结束");
            subtitleLabel.setText("下次好运！");
        }

        // 设置分数
        scoreLabel.setText(String.valueOf(gameState.getScore()));

        // 设置统计信息
        long gameDuration = gameState.getGameDuration();
        timePlayedLabel.setText(formatTime(gameDuration));
        skillsEarnedLabel.setText(String.valueOf(gameState.getSkillsEarned()));
        foodConsumedLabel.setText(String.valueOf(foodConsumed));
        maxLengthLabel.setText(String.valueOf(maxSnakeLength));

        // 如果获得了技能，设置技能总结
        if (gameState.getSkillsEarned() > 0) {
            skillsSummarySection.setVisible(true);
            skillsSummaryLabel.setText(gameState.getSkillsEarned() + " / " + GameState.getMaxSkills());

            // 找到获得的最高稀有度技能
            List<SkillCard> earnedSkills = gameState.getEarnedSkills();
            if (!earnedSkills.isEmpty()) {
                SkillCard bestSkill = earnedSkills.stream()
                    .max((s1, s2) -> Integer.compare(s1.getRarity(), s2.getRarity()))
                    .orElse(earnedSkills.get(0));
                bestSkillLabel.setText("最佳技能: " + bestSkill.getIcon() + " " + bestSkill.getName() +
                                     " (" + bestSkill.getRarityName() + ")");
            }
        }

        // 设置成就徽章
        setupAchievements();
    }
    
    /**
     * 根据游戏表现设置成就徽章
     */
    private void setupAchievements() {
        achievementContainer.getChildren().clear();
        boolean hasAchievements = false;

        // 完美得分成就（达到胜利条件）
        if (isWinScreen) {
            Label perfectBadge = createAchievementBadge("🏆 完美", "achievement-perfect");
            achievementContainer.getChildren().add(perfectBadge);
            hasAchievements = true;
        }

        // 技能大师成就（获得10+技能）
        if (gameState.getSkillsEarned() >= 10) {
            Label skillBadge = createAchievementBadge("🎯 技能大师", "achievement-skilled");
            achievementContainer.getChildren().add(skillBadge);
            hasAchievements = true;
        }

        // 生存者成就（游戏时间5+分钟）
        if (gameState.getGameDuration() >= 300) { // 5分钟
            Label survivorBadge = createAchievementBadge("⏱️ 生存者", "achievement-survivor");
            achievementContainer.getChildren().add(survivorBadge);
            hasAchievements = true;
        }

        // 高分成就（500+分）
        if (gameState.getScore() >= 500) {
            Label scoreBadge = createAchievementBadge("💯 高分", "achievement-badge");
            achievementContainer.getChildren().add(scoreBadge);
            hasAchievements = true;
        }

        achievementContainer.setVisible(hasAchievements);
    }
    
    /**
     * 创建成就徽章
     */
    private Label createAchievementBadge(String text, String styleClass) {
        Label badge = new Label(text);
        badge.getStyleClass().addAll("achievement-badge", styleClass);
        return badge;
    }

    /**
     * 将时间长度格式化为MM:SS格式
     */
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
    
    /**
     * Play entry animations for a polished appearance
     */
    private void playEntryAnimations() {
        // Title fade-in animation
        FadeTransition titleFade = new FadeTransition(Duration.millis(800), titleLabel);
        titleFade.setFromValue(0.0);
        titleFade.setToValue(1.0);
        
        // Scale animation for title
        ScaleTransition titleScale = new ScaleTransition(Duration.millis(800), titleLabel);
        titleScale.setFromX(0.5);
        titleScale.setFromY(0.5);
        titleScale.setToX(1.0);
        titleScale.setToY(1.0);
        
        // Subtitle slide-up animation
        TranslateTransition subtitleSlide = new TranslateTransition(Duration.millis(600), subtitleLabel);
        subtitleSlide.setFromY(30);
        subtitleSlide.setToY(0);
        
        FadeTransition subtitleFade = new FadeTransition(Duration.millis(600), subtitleLabel);
        subtitleFade.setFromValue(0.0);
        subtitleFade.setToValue(1.0);
        
        // Score section animation
        FadeTransition scoreFade = new FadeTransition(Duration.millis(1000), scoreLabel.getParent());
        scoreFade.setFromValue(0.0);
        scoreFade.setToValue(1.0);
        scoreFade.setDelay(Duration.millis(400));
        
        // Statistics animation
        FadeTransition statsFade = new FadeTransition(Duration.millis(800), timePlayedLabel.getParent());
        statsFade.setFromValue(0.0);
        statsFade.setToValue(1.0);
        statsFade.setDelay(Duration.millis(600));
        
        // Buttons animation
        FadeTransition buttonsFade = new FadeTransition(Duration.millis(600), restartButton.getParent());
        buttonsFade.setFromValue(0.0);
        buttonsFade.setToValue(1.0);
        buttonsFade.setDelay(Duration.millis(800));
        
        // Play all animations
        ParallelTransition titleAnimation = new ParallelTransition(titleFade, titleScale);
        ParallelTransition subtitleAnimation = new ParallelTransition(subtitleSlide, subtitleFade);
        
        SequentialTransition fullAnimation = new SequentialTransition(
            titleAnimation,
            subtitleAnimation,
            scoreFade,
            statsFade,
            buttonsFade
        );
        
        fullAnimation.play();
        
        // Achievement badges animation (if visible)
        if (achievementContainer.isVisible()) {
            FadeTransition achievementFade = new FadeTransition(Duration.millis(500), achievementContainer);
            achievementFade.setFromValue(0.0);
            achievementFade.setToValue(1.0);
            achievementFade.setDelay(Duration.millis(1200));
            achievementFade.play();
        }
        
        // Skills summary animation (if visible)
        if (skillsSummarySection.isVisible()) {
            FadeTransition skillsFade = new FadeTransition(Duration.millis(500), skillsSummarySection);
            skillsFade.setFromValue(0.0);
            skillsFade.setToValue(1.0);
            skillsFade.setDelay(Duration.millis(1000));
            skillsFade.play();
        }
    }
    
    /**
     * Handle keyboard input
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onRestartClicked();
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            onExitClicked();
            event.consume();
        }
    }
    
    /**
     * Handle restart button click
     */
    @FXML
    private void onRestartClicked() {
        if (onRestartCallback != null) {
            // Play button press animation
            playButtonPressAnimation(restartButton, onRestartCallback);
        }
    }
    
    /**
     * Handle exit button click
     */
    @FXML
    private void onExitClicked() {
        if (onExitCallback != null) {
            // Play button press animation
            playButtonPressAnimation(exitButton, onExitCallback);
        }
    }
    
    /**
     * Play button press animation before executing callback
     */
    private void playButtonPressAnimation(Button button, Runnable callback) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        
        SequentialTransition buttonAnimation = new SequentialTransition(scaleDown, scaleUp);
        buttonAnimation.setOnFinished(e -> callback.run());
        buttonAnimation.play();
    }
}
