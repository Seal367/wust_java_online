package com.example.javafx3.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.example.javafx3.manager.GameState;
import com.example.javafx3.manager.SkillManager;
import com.example.javafx3.model.SkillCard;
import com.example.javafx3.model.SkillEffect;
import com.example.javafx3.ui.SkillNotificationSystem;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Controller for the skill management menu
 */
public class SkillMenuController implements Initializable {
    
    @FXML private Button closeButton;
    @FXML private Label scoreLabel;
    @FXML private Label skillsEarnedLabel;
    @FXML private Label activeSlotsLabel;
    @FXML private ProgressBar skillProgressBar;
    @FXML private Label skillProgressLabel;
    @FXML private ScrollPane activeSkillsPane;
    @FXML private FlowPane activeSkillsContainer;
    @FXML private ComboBox<String> rarityFilter;
    @FXML private ScrollPane availableSkillsPane;
    @FXML private GridPane skillGrid;
    @FXML private Button activateSelectedButton;
    @FXML private Button deactivateAllButton;
    @FXML private Button resetSkillsButton;
    
    private GameState gameState;
    private SkillManager skillManager;
    private SkillNotificationSystem notificationSystem;
    private SkillCard selectedSkill;
    private Runnable onCloseCallback;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRarityFilter();
        setupUI();
    }
    
    /**
     * Initialize the controller with game state and skill manager
     */
    public void initializeController(GameState gameState, SkillManager skillManager,
                                   SkillNotificationSystem notificationSystem, Runnable onCloseCallback) {
        this.gameState = gameState;
        this.skillManager = skillManager;
        this.notificationSystem = notificationSystem;
        this.onCloseCallback = onCloseCallback;

        updateUI();
        loadSkillCards();
    }
    
    /**
     * Setup the rarity filter dropdown
     */
    private void setupRarityFilter() {
        rarityFilter.setItems(FXCollections.observableArrayList(
            "All Rarities", "Common", "Uncommon", "Rare", "Legendary"
        ));
        rarityFilter.setValue("All Rarities");
    }
    
    /**
     * Setup UI components
     */
    private void setupUI() {
        // Configure scroll panes
        activeSkillsPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        activeSkillsPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        availableSkillsPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        availableSkillsPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Configure skill grid
        skillGrid.setHgap(10);
        skillGrid.setVgap(10);
    }
    
    /**
     * Update UI with current game state
     */
    public void updateUI() {
        if (gameState == null) return;
        
        Platform.runLater(() -> {
            // Update labels
            scoreLabel.setText(String.valueOf(gameState.getScore()));
            skillsEarnedLabel.setText(gameState.getSkillsEarned() + "/" + GameState.getMaxSkills());
            activeSlotsLabel.setText(gameState.getUsedActivationSlots() + "/" + gameState.getMaxActivationSlots());
            
            // Update progress bar
            double progress = gameState.getSkillProgress();
            skillProgressBar.setProgress(progress);
            skillProgressLabel.setText(gameState.getPointsToNextSkill() + "/" + GameState.getPointsPerSkill() + " points");
            
            // Update active skills
            updateActiveSkills();
            
            // Update button states
            updateButtonStates();
        });
    }
    
    /**
     * Update active skills display
     */
    private void updateActiveSkills() {
        activeSkillsContainer.getChildren().clear();
        
        List<SkillCard> activeSkills = gameState.getActiveSkills();
        for (SkillCard skill : activeSkills) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/skill-card.fxml"));
                VBox skillCardNode = loader.load();
                SkillCardController controller = loader.getController();
                controller.setSkillCard(skill, true, this::onSkillCardSelected);
                
                activeSkillsContainer.getChildren().add(skillCardNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Load and display skill cards
     */
    private void loadSkillCards() {
        if (gameState == null) return;
        
        skillGrid.getChildren().clear();
        
        List<SkillCard> earnedSkills = getFilteredSkills();
        int columns = 3;
        int row = 0, col = 0;
        
        for (SkillCard skill : earnedSkills) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/skill-card.fxml"));
                VBox skillCardNode = loader.load();
                SkillCardController controller = loader.getController();
                controller.setSkillCard(skill, false, this::onSkillCardSelected);
                
                skillGrid.add(skillCardNode, col, row);
                
                col++;
                if (col >= columns) {
                    col = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get filtered skills based on rarity selection
     */
    private List<SkillCard> getFilteredSkills() {
        List<SkillCard> skills = gameState.getEarnedSkills();
        String selectedRarity = rarityFilter.getValue();
        
        if ("All Rarities".equals(selectedRarity)) {
            return skills;
        }
        
        return skills.stream()
                .filter(skill -> skill.getRarityName().equals(selectedRarity))
                .collect(Collectors.toList());
    }
    
    /**
     * Handle skill card selection
     */
    private void onSkillCardSelected(SkillCard skill) {
        selectedSkill = skill;
        updateButtonStates();
    }
    
    /**
     * Update button states based on current selection and game state
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedSkill != null;
        boolean canActivate = hasSelection && selectedSkill.canActivate() && 
                             gameState.getAvailableActivationSlots() > 0;
        boolean hasActiveSkills = !gameState.getActiveSkills().isEmpty();
        
        activateSelectedButton.setDisable(!canActivate);
        deactivateAllButton.setDisable(!hasActiveSkills);
        resetSkillsButton.setDisable(!gameState.isGameRunning());
    }
    
    @FXML
    private void closeMenu() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
    
    @FXML
    private void filterSkills() {
        loadSkillCards();
    }
    
    @FXML
    private void activateSelectedSkill() {
        if (selectedSkill != null && gameState != null) {
            // Check for conflicts first
            SkillManager.ConflictResolution conflict = skillManager.checkSkillConflicts(selectedSkill);

            if (conflict.hasConflict()) {
                // Handle conflict resolution
                int compensation = skillManager.resolveConflict(selectedSkill, conflict.getConflictingSkill());
                gameState.updateScore(compensation);

                if (notificationSystem != null) {
                    notificationSystem.showSkillConflict(conflict.getMessage() +
                        " (+" + compensation + " compensation points)");
                }
            }

            if (gameState.activateSkill(selectedSkill)) {
                updateUI();
                loadSkillCards();

                if (notificationSystem != null) {
                    notificationSystem.showSkillActivated(selectedSkill);
                }

                selectedSkill = null;
            } else {
                showNotification("Cannot activate skill!");
            }
        }
    }
    
    @FXML
    private void deactivateAllSkills() {
        // Deactivate all active skills via gameState
        List<SkillCard> activeSkills = new ArrayList<>(gameState.getActiveSkills());
        for (SkillCard skill : activeSkills) {
            gameState.removeActiveSkill(skill);
            skill.deactivate();
        }
        updateUI();
        showNotification("All skills deactivated");
    }
    
    @FXML
    private void resetSkills() {
        // This would reset all skills - implement with confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Skills");
        alert.setHeaderText("Reset All Skills?");
        alert.setContentText("This will deactivate all skills and reset their usage. Continue?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Reset logic would go here
                updateUI();
                showNotification("Skills reset");
            }
        });
    }
    
    /**
     * Show a notification message
     */
    private void showNotification(String message) {
        // This could be implemented as a toast notification
        System.out.println("Notification: " + message);
    }
}
