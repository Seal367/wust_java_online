package com.example.javafx3.ui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import com.example.javafx3.model.SkillCard;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 技能详情界面控制器
 */
public class SkillDetailController implements Initializable {
    
    @FXML private Label skillIconLabel;
    @FXML private Label skillNameLabel;
    @FXML private Label skillRarityLabel;
    @FXML private Label skillDescriptionLabel;
    @FXML private Label skillEffectsLabel;
    @FXML private Label skillDurationLabel;
    @FXML private Label skillCooldownLabel;
    @FXML private Label skillRarityInfoLabel;
    @FXML private Button selectSkillButton;
    @FXML private Button backButton;
    
    private SkillCard currentSkill;
    private Runnable onSelectCallback;
    private Runnable onBackCallback;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置键盘事件处理
        Platform.runLater(() -> {
            if (selectSkillButton.getScene() != null) {
                selectSkillButton.getScene().setOnKeyPressed(this::handleKeyPress);
            }
        });
        
        // 设置初始焦点
        Platform.runLater(() -> selectSkillButton.requestFocus());
    }
    
    /**
     * 初始化控制器
     */
    public void initializeController(SkillCard skill, Runnable onSelectCallback, Runnable onBackCallback) {
        this.currentSkill = skill;
        this.onSelectCallback = onSelectCallback;
        this.onBackCallback = onBackCallback;
        
        setupSkillDisplay();
        playEntryAnimation();
    }
    
    /**
     * 设置技能显示内容
     */
    private void setupSkillDisplay() {
        if (currentSkill == null) return;
        
        // 设置基本信息
        skillIconLabel.setText(currentSkill.getIcon());
        skillNameLabel.setText(currentSkill.getName());
        skillRarityLabel.setText(currentSkill.getRarityName());
        
        // 应用稀有度样式
        String rarityClass = "rarity-" + currentSkill.getRarityName().toLowerCase();
        skillRarityLabel.getStyleClass().add(rarityClass);
        
        // 设置描述
        skillDescriptionLabel.setText(currentSkill.getDescription());
        
        // 设置效果详情
        String effectsText = getDetailedEffectsText(currentSkill);
        skillEffectsLabel.setText(effectsText);
        
        // 设置参数信息
        setupParametersInfo();
        
        // 设置稀有度信息
        setupRarityInfo();
    }
    
    /**
     * 获取详细效果文本
     */
    private String getDetailedEffectsText(SkillCard skill) {
        StringBuilder effects = new StringBuilder();
        
        switch (skill.getName()) {
            case "速度提升":
                effects.append("• 蛇的移动速度增加50%\n");
                effects.append("• 更快的反应时间和游戏节奏\n");
                effects.append("• 适合快速收集食物");
                break;
            case "双倍食物":
                effects.append("• 同时在场上生成两个食物\n");
                effects.append("• 大幅提高得分效率\n");
                effects.append("• 增加游戏策略性");
                break;
            case "分数狂热":
                effects.append("• 每次吃食物获得双倍分数\n");
                effects.append("• 连击系统激活\n");
                effects.append("• 快速提升总分数");
                break;
            case "幽灵模式":
                effects.append("• 可以穿过自己的身体\n");
                effects.append("• 避免自撞死亡\n");
                effects.append("• 增加生存能力");
                break;
            case "第二次机会":
                effects.append("• 死亡时自动复活一次\n");
                effects.append("• 保留当前分数和长度\n");
                effects.append("• 珍贵的保险技能");
                break;
            case "穿墙术":
                effects.append("• 可以穿过游戏边界\n");
                effects.append("• 从对面墙壁出现\n");
                effects.append("• 增加移动灵活性");
                break;
            case "时间倒流":
                effects.append("• 将蛇恢复到3秒前的状态\n");
                effects.append("• 撤销错误操作\n");
                effects.append("• 强大的救命技能");
                break;
            case "时间冻结":
                effects.append("• 游戏速度减慢90%\n");
                effects.append("• 更容易精确控制\n");
                effects.append("• 适合复杂操作");
                break;
            case "无敌模式":
                effects.append("• 短时间内免疫所有伤害\n");
                effects.append("• 可以安全穿过障碍\n");
                effects.append("• 终极防护技能");
                break;
            case "瞬移":
                effects.append("• 立即传送到安全位置\n");
                effects.append("• 避免即将发生的碰撞\n");
                effects.append("• 紧急逃脱技能");
                break;
            default:
                effects.append("• ").append(skill.getDescription());
        }
        
        return effects.toString();
    }
    
    /**
     * 设置参数信息
     */
    private void setupParametersInfo() {
        // 根据技能类型设置持续时间和冷却时间
        String duration = "即时";
        String cooldown = "无";
        
        switch (currentSkill.getName()) {
            case "速度提升":
            case "双倍食物":
            case "分数狂热":
                duration = "15秒";
                cooldown = "30秒";
                break;
            case "幽灵模式":
            case "时间冻结":
                duration = "10秒";
                cooldown = "45秒";
                break;
            case "无敌模式":
                duration = "5秒";
                cooldown = "60秒";
                break;
            case "第二次机会":
            case "穿墙术":
            case "时间倒流":
            case "瞬移":
                duration = "即时";
                cooldown = "一次性";
                break;
        }
        
        skillDurationLabel.setText(duration);
        skillCooldownLabel.setText(cooldown);
    }
    
    /**
     * 设置稀有度信息
     */
    private void setupRarityInfo() {
        String rarityInfo = "";
        
        switch (currentSkill.getRarity()) {
            case 1: // 普通
                rarityInfo = "普通技能 - 基础效果，容易获得，适合新手使用。";
                break;
            case 2: // 不常见
                rarityInfo = "不常见技能 - 增强效果，获得概率中等，提供明显优势。";
                break;
            case 3: // 稀有
                rarityInfo = "稀有技能 - 强力效果，获得概率较低，显著改变游戏体验。";
                break;
            case 4: // 史诗
                rarityInfo = "史诗技能 - 极强效果，获得概率很低，提供巨大战略优势。";
                break;
            case 5: // 传说
                rarityInfo = "传说技能 - 顶级效果，极其罕见，可以扭转游戏局势。";
                break;
            case 6: // 神话
                rarityInfo = "神话技能 - 终极效果，传说中的技能，拥有改变一切的力量。";
                break;
        }
        
        skillRarityInfoLabel.setText(rarityInfo);
    }
    
    /**
     * 播放进入动画
     */
    private void playEntryAnimation() {
        // 技能图标缩放动画
        ScaleTransition iconScale = new ScaleTransition(Duration.millis(600), skillIconLabel);
        iconScale.setFromX(0.5);
        iconScale.setFromY(0.5);
        iconScale.setToX(1.0);
        iconScale.setToY(1.0);
        
        // 技能名称淡入动画
        FadeTransition nameFade = new FadeTransition(Duration.millis(800), skillNameLabel);
        nameFade.setFromValue(0.0);
        nameFade.setToValue(1.0);
        
        // 内容区域滑入动画
        TranslateTransition contentSlide = new TranslateTransition(Duration.millis(600), skillDescriptionLabel.getParent());
        contentSlide.setFromY(30);
        contentSlide.setToY(0);
        
        FadeTransition contentFade = new FadeTransition(Duration.millis(600), skillDescriptionLabel.getParent());
        contentFade.setFromValue(0.0);
        contentFade.setToValue(1.0);
        
        // 按钮动画
        FadeTransition buttonFade = new FadeTransition(Duration.millis(500), selectSkillButton.getParent());
        buttonFade.setFromValue(0.0);
        buttonFade.setToValue(1.0);
        buttonFade.setDelay(Duration.millis(400));
        
        // 播放所有动画
        ParallelTransition headerAnimation = new ParallelTransition(iconScale, nameFade);
        ParallelTransition contentAnimation = new ParallelTransition(contentSlide, contentFade);
        
        SequentialTransition fullAnimation = new SequentialTransition(
            headerAnimation,
            contentAnimation,
            buttonFade
        );
        
        fullAnimation.play();
    }
    
    /**
     * 处理键盘输入
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onSelectSkill();
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            onBack();
            event.consume();
        }
    }
    
    /**
     * 选择技能
     */
    @FXML
    private void onSelectSkill() {
        if (onSelectCallback != null) {
            // 播放按钮按下动画
            playButtonPressAnimation(selectSkillButton, onSelectCallback);
        }
    }
    
    /**
     * 返回选择
     */
    @FXML
    private void onBack() {
        if (onBackCallback != null) {
            // 播放按钮按下动画
            playButtonPressAnimation(backButton, onBackCallback);
        }
    }
    
    /**
     * 播放按钮按下动画
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
