package com.example.javafx3.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import com.example.javafx3.model.SkillCard;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Controller for individual skill card display
 */
public class SkillCardController implements Initializable {
    
    @FXML private VBox cardContainer;
    @FXML private Label skillIcon;
    @FXML private Label skillName;
    @FXML private Label skillDescription;
    @FXML private Label rarityStars;
    @FXML private Label skillRarity;
    @FXML private Label skillStatus;
    @FXML private ProgressBar activationProgress;
    @FXML private Label durationTimer;
    
    private SkillCard skillCard;
    private boolean isActiveSkillDisplay;
    private Consumer<SkillCard> onCardClickCallback;
    private Timeline updateTimer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UI components
        setupUI();
    }
    
    /**
     * Setup UI components
     */
    private void setupUI() {
        // Set default visibility
        activationProgress.setVisible(false);
        activationProgress.setManaged(false);
        durationTimer.setVisible(false);
        durationTimer.setManaged(false);
    }
    
    /**
     * Set the skill card data and configure the display
     */
    public void setSkillCard(SkillCard skillCard, boolean isActiveDisplay, Consumer<SkillCard> onClickCallback) {
        this.skillCard = skillCard;
        this.isActiveSkillDisplay = isActiveDisplay;
        this.onCardClickCallback = onClickCallback;
        
        updateDisplay();
        setupUpdateTimer();
    }
    
    /**
     * Update the card display with current skill data
     */
    private void updateDisplay() {
        if (skillCard == null) return;
        
        Platform.runLater(() -> {
            // Basic skill information
            skillIcon.setText(skillCard.getIcon());
            skillName.setText(skillCard.getName());
            skillDescription.setText(skillCard.getDescription());
            skillRarity.setText(skillCard.getRarityName());
            
            // Set rarity color
            skillRarity.setStyle("-fx-text-fill: " + skillCard.getRarityColor() + ";");
            
            // Set rarity stars
            String stars = generateRarityStars(skillCard.getRarity());
            rarityStars.setText(stars);
            rarityStars.setStyle("-fx-text-fill: " + skillCard.getRarityColor() + ";");
            
            // Update card styling based on rarity
            updateCardStyling();
            
            // Update status and progress
            updateStatusDisplay();
            
            // Update duration timer for active skills
            updateDurationDisplay();
        });
    }
    
    /**
     * Generate star display for rarity
     */
    private String generateRarityStars(int rarity) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rarity; i++) {
            stars.append("★");
        }
        return stars.toString();
    }
    
    /**
     * Update card styling based on rarity and state
     */
    private void updateCardStyling() {
        cardContainer.getStyleClass().removeAll(
            "skill-card-common", "skill-card-uncommon", 
            "skill-card-rare", "skill-card-legendary",
            "skill-active", "skill-used", "skill-cooldown"
        );
        
        // Add rarity styling
        switch (skillCard.getRarity()) {
            case 1:
                cardContainer.getStyleClass().add("skill-card-common");
                break;
            case 2:
                cardContainer.getStyleClass().add("skill-card-uncommon");
                break;
            case 3:
                cardContainer.getStyleClass().add("skill-card-rare");
                break;
            case 4:
                cardContainer.getStyleClass().add("skill-card-legendary");
                break;
        }
        
        // Add state styling
        if (skillCard.isActive()) {
            cardContainer.getStyleClass().add("skill-active");
        } else if (skillCard.getRemainingActivations() <= 0) {
            cardContainer.getStyleClass().add("skill-used");
        }
    }
    
    /**
     * Update status display
     */
    private void updateStatusDisplay() {
        String statusText;
        boolean showProgress = false;
        
        if (skillCard.isActive()) {
            if (skillCard.getRemainingDuration() > 0) {
                statusText = "Active (" + skillCard.getRemainingDuration() + "s)";
                showProgress = true;
            } else {
                statusText = "Active";
            }
        } else if (skillCard.getRemainingActivations() > 0) {
            statusText = "Available (" + skillCard.getRemainingActivations() + " uses)";
        } else {
            statusText = "Used";
        }
        
        skillStatus.setText(statusText);
        
        // Show/hide progress bar
        activationProgress.setVisible(showProgress);
        activationProgress.setManaged(showProgress);
        
        if (showProgress && skillCard.getDuration() > 0) {
            long elapsed = System.currentTimeMillis() - skillCard.getActivationTime();
            double progress = 1.0 - ((double) elapsed / skillCard.getDuration());
            activationProgress.setProgress(Math.max(0, Math.min(1, progress)));
        }
    }
    
    /**
     * Update duration display for active skills
     */
    private void updateDurationDisplay() {
        boolean showTimer = isActiveSkillDisplay && skillCard.isActive() && skillCard.getRemainingDuration() > 0;
        
        durationTimer.setVisible(showTimer);
        durationTimer.setManaged(showTimer);
        
        if (showTimer) {
            int remaining = skillCard.getRemainingDuration();
            durationTimer.setText(formatDuration(remaining));
        }
    }
    
    /**
     * Format duration for display
     */
    private String formatDuration(int seconds) {
        if (seconds >= 60) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%d:%02d", minutes, remainingSeconds);
        } else {
            return seconds + "s";
        }
    }
    
    /**
     * Setup timer for updating active skill displays
     */
    private void setupUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        
        // Only setup timer for active skills or skills with duration
        if (skillCard.isActive() || isActiveSkillDisplay) {
            updateTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDisplay()));
            updateTimer.setCycleCount(Timeline.INDEFINITE);
            updateTimer.play();
        }
    }
    
    /**
     * Handle card click events
     */
    @FXML
    private void onCardClicked(MouseEvent event) {
        if (onCardClickCallback != null && skillCard != null) {
            onCardClickCallback.accept(skillCard);
            
            // Visual feedback for selection
            cardContainer.getStyleClass().add("skill-card-selected");
            
            // Remove selection styling after a short delay
            Timeline selectionFeedback = new Timeline(new KeyFrame(Duration.millis(200), e -> {
                cardContainer.getStyleClass().remove("skill-card-selected");
            }));
            selectionFeedback.play();
        }
    }
    
    /**
     * Cleanup resources when card is no longer needed
     */
    public void cleanup() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
        }
    }
    
    /**
     * Get the associated skill card
     */
    public SkillCard getSkillCard() {
        return skillCard;
    }
    
    /**
     * Check if this is an active skill display
     */
    public boolean isActiveSkillDisplay() {
        return isActiveSkillDisplay;
    }
}
