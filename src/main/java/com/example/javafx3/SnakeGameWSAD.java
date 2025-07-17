package com.example.javafx3;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import com.example.javafx3.manager.GameState;
import com.example.javafx3.manager.SkillManager;
import com.example.javafx3.model.SkillCard;
import com.example.javafx3.model.SkillEffect;
import com.example.javafx3.ui.SkillMenuController;
import com.example.javafx3.ui.SkillNotificationSystem;
import com.example.javafx3.ui.GameOverController;
import com.example.javafx3.ui.SkillDetailController;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SnakeGameWSAD extends Application implements SkillManager.SnakeGameEffects {

    // 游戏常量 - 增大窗口尺寸以确保游戏结束界面完整显示
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int CELL_SIZE = 20;
    private static final int GRID_WIDTH = WIDTH / CELL_SIZE;
    private static final int GRID_HEIGHT = (HEIGHT - 150) / CELL_SIZE; // 增加更多UI面板空间
    private static final int INITIAL_SPEED = 150; // 毫秒

    // 游戏变量
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction currentDirection = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private Deque<Point> snake = new ArrayDeque<>();
    private Point food;
    private List<Point> additionalFood = new ArrayList<>(); // For double food skill
    private boolean gameRunning = false;
    private boolean gameOver = false;
    private int score = 0;
    private long lastUpdate = 0;
    private int speed = INITIAL_SPEED;

    // 方向缓冲区 - 解决键盘响应问题
    private final LinkedList<Direction> directionBuffer = new LinkedList<>();
    private static final int BUFFER_SIZE = 2;

    // 游戏组件
    private Canvas gameCanvas;
    private GraphicsContext gc;
    private Label scoreLabel;
    private Label gameStatus;
    private Button startButton;
    private Button skillMenuButton;
    private Label focusLabel; // 焦点状态标签
    private ProgressBar skillProgressBar;
    private Label skillProgressLabel;

    // Skill system components
    private GameState gameState;
    private SkillManager skillManager;
    private SkillNotificationSystem notificationSystem;
    private Stage skillMenuStage;
    private Stage gameOverStage;
    private Stage skillDetailStage;

    // Game statistics tracking
    private int maxSnakeLength = 3;
    private int foodConsumed = 0;
    private Stage primaryStage;

    // Comprehensive skill effects state
    private double speedMultiplier = 1.0;
    private double scoreMultiplier = 1.0;
    private int foodMagnetRadius = 0;
    private boolean wallPhasingEnabled = false;
    private boolean ghostModeEnabled = false;
    private boolean doubleFood = false;
    private boolean basicShieldEnabled = false;
    private boolean steelBodyEnabled = false;
    private boolean secondChanceEnabled = false;
    private boolean invincibilityEnabled = false;
    private boolean timeFreezeEnabled = false;
    private boolean timeRewindEnabled = false;
    private boolean luckyStarEnabled = false;
    private boolean scoreFrenzyEnabled = false;
    private boolean worldShrinkEnabled = false;
    private double worldShrinkFactor = 1.0;
    private int shieldUses = 0;

    @Override
    public void start(Stage primaryStage) {
        // Save primary stage reference
        this.primaryStage = primaryStage;

        // Initialize skill system
        gameState = new GameState();
        skillManager = new SkillManager(gameState);

        // Load CSS styles
        try {
            // Preload CSS files to ensure they're available when needed
            getClass().getResource("/css/game-styles.css");
            getClass().getResource("/css/skill-ui.css");
            getClass().getResource("/css/game-over.css");
        } catch (Exception e) {
            System.out.println("Warning: Could not load CSS files: " + e.getMessage());
        }

        // 创建主布局
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        // Initialize notification system
        notificationSystem = new SkillNotificationSystem(root);

        // 创建游戏画布
        gameCanvas = new Canvas(WIDTH, HEIGHT - 150);
        gc = gameCanvas.getGraphicsContext2D();

        // 确保画布可以获得焦点
        gameCanvas.setFocusTraversable(true);

        // 创建控制面板
        HBox controlPanel = createControlPanel();

        // 创建状态面板
        HBox statusPanel = createStatusPanel();

        // 组装UI
        root.setCenter(gameCanvas);
        root.setBottom(controlPanel);
        root.setTop(statusPanel);

        // 设置场景
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.BLACK);

        // 键盘事件处理 - 监听WSAD键、空格键启动游戏和技能菜单键
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W ||
                    e.getCode() == KeyCode.S ||
                    e.getCode() == KeyCode.A ||
                    e.getCode() == KeyCode.D) {
                handleKeyPress(e.getCode());
                e.consume(); // 阻止事件继续传播
            } else if (e.getCode() == KeyCode.TAB && gameRunning) {
                openSkillMenu();
                e.consume();
            } else if (e.getCode() == KeyCode.SPACE && !gameRunning) {
                // 空格键启动游戏
                startGame();
                e.consume();
            }
        });

        // 初始化游戏
        initializeGame();

        // 设置舞台
        primaryStage.setTitle("JavaFX 贪吃蛇游戏 - 技能增强版 (WSAD控制)");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // 确保开始按钮可见
        Platform.runLater(() -> {
            ensureStartButtonVisible();
            System.out.println("Game initialized and start button visibility ensured");
        });

        // 启动游戏循环
        startGameLoop();
    }

    // 创建控制面板
    private HBox createControlPanel() {
        HBox panel = new HBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(20));
        panel.setPrefHeight(80); // Ensure panel has enough height
        panel.setMinHeight(80);  // Set minimum height
        panel.setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        // 创建开始按钮
        startButton = new Button("开始游戏");
        startButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                           "-fx-padding: 10px 20px; -fx-border-radius: 5px; " +
                           "-fx-background-radius: 5px; -fx-cursor: hand; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 1);");
        startButton.setOnAction(e -> startGame());
        startButton.setFocusTraversable(true);
        startButton.setDefaultButton(true); // Make it the default button (Enter key)

        // Add hover effects for start button
        startButton.setOnMouseEntered(e -> {
            if (!gameRunning) {
                startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                                   "-fx-padding: 10px 20px; -fx-border-radius: 5px; " +
                                   "-fx-background-radius: 5px; -fx-cursor: hand; " +
                                   "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 4, 0, 0, 2);");
            }
        });
        startButton.setOnMouseExited(e -> {
            if (!gameRunning) {
                startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                                   "-fx-padding: 10px 20px; -fx-border-radius: 5px; " +
                                   "-fx-background-radius: 5px; -fx-cursor: hand; " +
                                   "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 1);");
            }
        });

        // 创建技能菜单按钮
        skillMenuButton = new Button("技能菜单 (Tab)");
        skillMenuButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        skillMenuButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; " +
                               "-fx-padding: 10px 20px; -fx-border-radius: 5px; " +
                               "-fx-background-radius: 5px; -fx-cursor: hand; " +
                               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 1);");
        skillMenuButton.setOnAction(e -> openSkillMenu());
        skillMenuButton.setDisable(true);

        // 创建退出按钮
        Button exitButton = new Button("退出游戏");
        exitButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        exitButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                          "-fx-padding: 10px 20px; -fx-border-radius: 5px; " +
                          "-fx-background-radius: 5px; -fx-cursor: hand; " +
                          "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 1);");
        exitButton.setOnAction(e -> System.exit(0));

        // 添加控制说明
        Label controlLabel = new Label("控制: W-上  S-下  A-左  D-右  Tab-技能  空格-开始");
        controlLabel.setTextFill(Color.LIGHTGRAY);
        controlLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        panel.getChildren().addAll(startButton, skillMenuButton, exitButton, controlLabel);
        return panel;
    }

    // 创建状态面板
    private HBox createStatusPanel() {
        HBox panel = new HBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(15));
        panel.setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        scoreLabel = new Label("分数: 0");
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        gameStatus = new Label("按开始按钮开始游戏");
        gameStatus.setTextFill(Color.GOLD);
        gameStatus.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // 技能进度显示
        VBox skillProgressContainer = new VBox(5);
        skillProgressContainer.setAlignment(Pos.CENTER);

        Label skillProgressTitle = new Label("下个技能");
        skillProgressTitle.setTextFill(Color.LIGHTBLUE);
        skillProgressTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        skillProgressBar = new ProgressBar(0);
        skillProgressBar.setPrefWidth(120);
        skillProgressBar.setStyle("-fx-accent: #3498db;");

        skillProgressLabel = new Label("0/60");
        skillProgressLabel.setTextFill(Color.LIGHTBLUE);
        skillProgressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        skillProgressContainer.getChildren().addAll(skillProgressTitle, skillProgressBar, skillProgressLabel);

        // 焦点状态显示
        focusLabel = new Label("焦点: 未获得");
        focusLabel.setTextFill(Color.CYAN);
        focusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // 兼容旧版本的焦点绑定
        gameCanvas.focusedProperty().addListener((obs, oldVal, newVal) -> {
            focusLabel.setText(newVal ? "焦点: 已获得" : "焦点: 未获得");
        });

        panel.getChildren().addAll(scoreLabel, gameStatus, skillProgressContainer, focusLabel);
        return panel;
    }

    // 初始化游戏
    private void initializeGame() {
        // 清空方向缓冲区
        directionBuffer.clear();

        // 创建初始蛇身 (3个部分)
        snake.clear();
        snake.add(new Point(GRID_WIDTH / 2, GRID_HEIGHT / 2));
        snake.add(new Point(GRID_WIDTH / 2 - 1, GRID_HEIGHT / 2));
        snake.add(new Point(GRID_WIDTH / 2 - 2, GRID_HEIGHT / 2));

        // 生成食物
        generateFood();

        // 重置游戏状态
        currentDirection = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        score = 0;
        speed = INITIAL_SPEED;
        gameOver = false;
        gameRunning = false;

        // 重置技能系统
        gameState.reset();
        skillManager.reset();
        resetSkillEffects();

        // Reset statistics tracking
        maxSnakeLength = 3;
        foodConsumed = 0;

        // Close game over screen if open
        if (gameOverStage != null && gameOverStage.isShowing()) {
            gameOverStage.close();
            gameOverStage = null;
        }

        // Close skill detail screen if open
        if (skillDetailStage != null && skillDetailStage.isShowing()) {
            skillDetailStage.close();
            skillDetailStage = null;
        }

        // 更新UI
        updateScore();
        updateSkillProgress();
        gameStatus.setText("按空格键或点击开始按钮开始游戏");
        startButton.setText("开始游戏");
        startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                           "-fx-padding: 10px 20px; -fx-border-radius: 5px; " +
                           "-fx-background-radius: 5px; -fx-cursor: hand; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 1);");
        startButton.setDisable(false); // Ensure button is enabled
        skillMenuButton.setDisable(true);

        // 绘制初始状态
        drawGame();

        // 确保按钮获得焦点和可见性
        Platform.runLater(() -> {
            ensureStartButtonVisible();
            startButton.requestFocus();
        });
    }

    // 开始游戏
    private void startGame() {
        System.out.println("Start button clicked! Starting game...");

        if (gameOver) {
            System.out.println("Game was over, reinitializing...");
            initializeGame();
        }

        gameRunning = true;
        gameOver = false;
        gameState.startGame();
        System.out.println("Game started successfully!");
        gameStatus.setText("游戏进行中...");
        startButton.setText("重新开始");
        startButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                           "-fx-padding: 10px 20px; -fx-border-radius: 5px; " +
                           "-fx-background-radius: 5px; -fx-cursor: hand; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 1);");
        skillMenuButton.setDisable(false);

        // 确保画布获得焦点
        Platform.runLater(() -> {
            gameCanvas.requestFocus();

            // 添加短暂延迟确保焦点稳定
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> gameCanvas.requestFocus());
            }).start();
        });
    }

    // 处理键盘输入 - 只处理WSAD
    private void handleKeyPress(KeyCode keyCode) {
        if (!gameRunning) return;

        Direction newDirection = null;

        switch (keyCode) {
            case W:
                newDirection = Direction.UP;
                break;
            case S:
                newDirection = Direction.DOWN;
                break;
            case A:
                newDirection = Direction.LEFT;
                break;
            case D:
                newDirection = Direction.RIGHT;
                break;
        }

        if (newDirection != null) {
            // 检查方向是否有效（不能直接反向）
            if ((currentDirection == Direction.UP && newDirection != Direction.DOWN) ||
                    (currentDirection == Direction.DOWN && newDirection != Direction.UP) ||
                    (currentDirection == Direction.LEFT && newDirection != Direction.RIGHT) ||
                    (currentDirection == Direction.RIGHT && newDirection != Direction.LEFT)) {

                // 添加到缓冲区（最多存储2个方向）
                if (directionBuffer.size() >= BUFFER_SIZE) {
                    directionBuffer.pollFirst(); // 移除最旧的指令
                }
                directionBuffer.addLast(newDirection);
            }
        }
    }

    // 生成食物
    private void generateFood() {
        Random random = new Random();
        int x, y;
        boolean onSnake;

        // 确保食物不会生成在蛇身上
        do {
            onSnake = false;
            x = random.nextInt(GRID_WIDTH);
            y = random.nextInt(GRID_HEIGHT);

            for (Point p : snake) {
                if (p.x == x && p.y == y) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);

        food = new Point(x, y);
    }

    // 更新游戏状态
    private void updateGame() {
        if (!gameRunning || gameOver) return;

        // Update active skills
        gameState.updateActiveSkills();

        // Apply skill effects
        skillManager.applySkillEffects(this);

        // 处理方向缓冲区
        if (!directionBuffer.isEmpty()) {
            nextDirection = directionBuffer.pollFirst();
        }

        // 应用方向变化
        currentDirection = nextDirection;

        // 计算蛇头的新位置
        Point head = snake.getFirst();
        Point newHead = new Point(head.x, head.y);

        // Apply food magnet effect
        if (foodMagnetRadius > 0) {
            newHead = applyFoodMagnet(newHead);
        }

        switch (currentDirection) {
            case UP: newHead.y--; break;
            case DOWN: newHead.y++; break;
            case LEFT: newHead.x--; break;
            case RIGHT: newHead.x++; break;
        }

        // 检查是否撞墙 (with wall phasing skill)
        if (newHead.x < 0 || newHead.x >= GRID_WIDTH ||
                newHead.y < 0 || newHead.y >= GRID_HEIGHT) {
            if (wallPhasingEnabled) {
                // Wrap around to opposite side
                if (newHead.x < 0) newHead.x = GRID_WIDTH - 1;
                if (newHead.x >= GRID_WIDTH) newHead.x = 0;
                if (newHead.y < 0) newHead.y = GRID_HEIGHT - 1;
                if (newHead.y >= GRID_HEIGHT) newHead.y = 0;
                wallPhasingEnabled = false; // One-time use
            } else if (!invincibilityEnabled) {
                handleCollision();
                return;
            }
        }

        // 检查是否撞到自己 (with comprehensive collision protection)
        if (!ghostModeEnabled && !steelBodyEnabled && !invincibilityEnabled) {
            for (Point p : snake) {
                if (p.x == newHead.x && p.y == newHead.y) {
                    handleCollision();
                    return;
                }
            }
        } else if (ghostModeEnabled) {
            // Ghost mode: 50% collision volume reduction (simplified as occasional pass-through)
            if (Math.random() > 0.5) {
                for (Point p : snake) {
                    if (p.x == newHead.x && p.y == newHead.y) {
                        handleCollision();
                        return;
                    }
                }
            }
        }

        // 移动蛇
        snake.addFirst(newHead);

        // 检查是否吃到食物
        boolean ateFood = false;
        if (newHead.x == food.x && newHead.y == food.y) {
            ateFood = true;
            handleFoodConsumption();
        }

        // Check additional food (for double food skill)
        for (int i = additionalFood.size() - 1; i >= 0; i--) {
            Point extraFood = additionalFood.get(i);
            if (newHead.x == extraFood.x && newHead.y == extraFood.y) {
                ateFood = true;
                additionalFood.remove(i);
                handleFoodConsumption();
                break;
            }
        }

        if (!ateFood) {
            // 没吃到食物，移除尾部
            snake.removeLast();
        }

        // 绘制游戏
        drawGame();
    }

    // Handle collision with comprehensive shield system
    private void handleCollision() {
        // Check second chance first (most powerful)
        if (secondChanceEnabled) {
            secondChanceEnabled = false;
            notificationSystem.showGameNotification("Second Chance", "Revived from death!", "💖");
            return;
        }

        // Check basic shield
        if (basicShieldEnabled && shieldUses > 0) {
            shieldUses--;
            if (shieldUses <= 0) {
                basicShieldEnabled = false;
            }
            notificationSystem.showGameNotification("Shield", "Collision blocked!", "🛡");
            return;
        }

        // Check steel body (self-collision immunity)
        if (steelBodyEnabled) {
            // Steel body only protects against self-collision, not walls
            // This would be handled in the collision detection logic
            return;
        }

        gameOver();
    }

    // 游戏结束
    private void gameOver() {
        gameOver = true;
        gameRunning = false;
        gameState.endGameWithLoss();

        // Show game over screen
        showGameOverScreen(false);
    }

    // Handle game win condition
    private void handleGameWin() {
        gameOver = true;
        gameRunning = false;

        // Show game over screen with win condition
        showGameOverScreen(true);
    }

    // 更新分数显示
    private void updateScore() {
        scoreLabel.setText("分数: " + score);
        updateSkillProgress();
    }

    // Handle food consumption with comprehensive skill effects
    private void handleFoodConsumption() {
        // Track statistics
        foodConsumed++;
        maxSnakeLength = Math.max(maxSnakeLength, snake.size() + 1); // +1 because we're about to grow

        int basePoints = 10;

        // Apply score multiplier
        double totalMultiplier = scoreMultiplier;

        // Apply score frenzy if active
        if (scoreFrenzyEnabled) {
            totalMultiplier *= gameState.getScoreFrenzyMultiplier();
        }

        int points = (int)(basePoints * totalMultiplier);

        // Increment food counter for various systems
        gameState.incrementFoodCounter();

        // Check for time rewind trigger
        if (timeRewindEnabled && gameState.shouldTriggerTimeRewind()) {
            // Save current state before potential rewind
            saveGameStateSnapshot();
        }

        // Generate lucky star bonus food
        if (luckyStarEnabled && Math.random() < 0.25) { // 25% chance
            generateAdditionalFood();
            notificationSystem.showGameNotification("Lucky Star", "Bonus food spawned!", "⭐");
        }

        // Update score and check for skill unlock
        if (gameState.updateScore(points)) {
            // Player won!
            handleGameWin();
            return;
        }

        score = gameState.getScore();
        updateScore();

        // Check if player should earn a new skill
        if (gameState.shouldEarnSkill()) {
            SkillCard newSkill = skillManager.generateRandomSkill();
            if (newSkill != null) {
                // Check for duplicates
                if (skillManager.isDuplicateSkill(newSkill)) {
                    int bonusScore = skillManager.convertDuplicateToScore(newSkill);
                    gameState.updateScore(bonusScore);
                    score = gameState.getScore();
                    updateScore();
                    notificationSystem.showGameNotification("重复技能",
                        "转换为 " + bonusScore + " 奖励分数！", "💰");
                } else {
                    // 暂停游戏并显示技能详情界面
                    gameRunning = false;
                    showSkillDetailScreen(newSkill);
                }
            }
        }

        // 生成新食物
        generateFood();

        // Generate additional food if double food is active
        if (doubleFood && additionalFood.isEmpty()) {
            generateAdditionalFood();
        }

        // 增加速度 (modified by speed multiplier)
        int newSpeed = (int)(speed / speedMultiplier);
        if (newSpeed > 50) {
            speed = Math.max(50, newSpeed - 2);
        }
    }

    // 绘制游戏
    private void drawGame() {
        // 清除画布
        gc.clearRect(0, 0, WIDTH, HEIGHT - 150);

        // 绘制网格背景
        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRect(0, 0, WIDTH, HEIGHT - 150);

        // 绘制网格线
        gc.setStroke(Color.rgb(50, 50, 50));
        gc.setLineWidth(0.5);
        for (int x = 0; x <= WIDTH; x += CELL_SIZE) {
            gc.strokeLine(x, 0, x, HEIGHT - 150);
        }
        for (int y = 0; y <= HEIGHT - 150; y += CELL_SIZE) {
            gc.strokeLine(0, y, WIDTH, y);
        }

        // 绘制食物
        gc.setFill(Color.RED);
        gc.fillOval(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeOval(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // Draw additional food (for double food skill)
        for (Point extraFood : additionalFood) {
            gc.setFill(Color.ORANGE);
            gc.fillOval(extraFood.x * CELL_SIZE, extraFood.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(1);
            gc.strokeOval(extraFood.x * CELL_SIZE, extraFood.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // 绘制蛇 (with skill effects)
        int index = 0;
        for (Point p : snake) {
            // 蛇头
            if (index == 0) {
                if (invincibilityEnabled) {
                    gc.setFill(Color.GOLD); // Golden head for invincibility
                } else if (ghostModeEnabled) {
                    gc.setFill(Color.LIGHTBLUE); // Light blue for ghost mode
                } else {
                    gc.setFill(Color.GREEN);
                }
            }
            // 蛇身
            else {
                // 创建渐变效果
                double factor = 0.7 + (0.3 * index / snake.size());
                if (ghostModeEnabled) {
                    gc.setFill(Color.rgb(173, 216, 230, 0.7)); // Semi-transparent for ghost mode
                } else {
                    gc.setFill(Color.rgb(0, (int)(255 * factor), 0));
                }
            }

            gc.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

            index++;
        }

        // 绘制蛇眼睛
        if (!snake.isEmpty()) {
            Point head = snake.getFirst();
            gc.setFill(Color.BLACK);

            // 根据方向绘制眼睛位置
            int eyeSize = CELL_SIZE / 5;
            int offset = CELL_SIZE / 3;

            if (currentDirection == Direction.RIGHT || currentDirection == Direction.LEFT) {
                gc.fillOval(head.x * CELL_SIZE + offset, head.y * CELL_SIZE + offset, eyeSize, eyeSize);
                gc.fillOval(head.x * CELL_SIZE + offset, head.y * CELL_SIZE + CELL_SIZE - offset - eyeSize, eyeSize, eyeSize);
            } else {
                gc.fillOval(head.x * CELL_SIZE + offset, head.y * CELL_SIZE + offset, eyeSize, eyeSize);
                gc.fillOval(head.x * CELL_SIZE + CELL_SIZE - offset - eyeSize, head.y * CELL_SIZE + offset, eyeSize, eyeSize);
            }
        }

        // 绘制方向缓冲区状态和技能效果
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 14));
        String bufferText = "指令: ";
        for (Direction d : directionBuffer) {
            switch (d) {
                case UP: bufferText += "↑ "; break;
                case DOWN: bufferText += "↓ "; break;
                case LEFT: bufferText += "← "; break;
                case RIGHT: bufferText += "→ "; break;
            }
        }
        gc.fillText(bufferText, 10, 20);
        gc.fillText("当前方向: " + getDirectionSymbol(currentDirection), 10, 40);

        // Draw active skill effects
        drawActiveSkillEffects();

        // 游戏结束提示
        if (gameOver) {
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(0, 0, WIDTH, HEIGHT - 150);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            gc.fillText("游戏结束", WIDTH/2 - 100, (HEIGHT - 150)/2 - 30);

            gc.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            gc.fillText("分数: " + score, WIDTH/2 - 70, (HEIGHT - 150)/2 + 30);
        }

        // 游戏开始提示
        if (!gameRunning && !gameOver) {
            gc.setFill(Color.rgb(255, 255, 255, 0.9));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
            String startText = "按空格键开始游戏";
            double textWidth = startText.length() * 16; // 估算文本宽度
            gc.fillText(startText, WIDTH/2 - textWidth/2, (HEIGHT - 150)/2);

            // 添加闪烁效果
            long currentTime = System.currentTimeMillis();
            if ((currentTime / 500) % 2 == 0) { // 每500毫秒闪烁一次
                gc.setFill(Color.rgb(46, 204, 113, 0.8)); // 绿色提示
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                String hintText = "或点击开始按钮";
                double hintWidth = hintText.length() * 10;
                gc.fillText(hintText, WIDTH/2 - hintWidth/2, (HEIGHT - 150)/2 + 40);
            }
        }
    }

    // 获取方向符号
    private String getDirectionSymbol(Direction dir) {
        switch (dir) {
            case UP: return "↑";
            case DOWN: return "↓";
            case LEFT: return "←";
            case RIGHT: return "→";
            default: return "?";
        }
    }

    // 启动游戏循环
    private void startGameLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) lastUpdate = now;

                // 控制游戏更新速度 (modified by time freeze)
                long effectiveSpeed = timeFreezeEnabled ? speed * 10 : speed;
                if (now - lastUpdate >= effectiveSpeed * 1_000_000) {
                    updateGame();
                    lastUpdate = now;
                }
            }
        }.start();
    }

    // 游戏点类
    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // Draw active skill effects on screen
    private void drawActiveSkillEffects() {
        gc.setFont(Font.font("Arial", 12));
        int yOffset = 60;

        List<SkillCard> activeSkills = gameState.getActiveSkills();
        for (SkillCard skill : activeSkills) {
            if (skill.isActive()) {
                String effectText = skill.getIcon() + " " + skill.getName();
                if (skill.getRemainingDuration() > 0) {
                    effectText += " (" + skill.getRemainingDuration() + "s)";
                }

                gc.setFill(Color.color(1, 1, 1, 0.8));
                gc.fillText(effectText, 10, yOffset);
                yOffset += 15;
            }
        }

        // Show special effects
        if (timeFreezeEnabled) {
            gc.setFill(Color.LIGHTBLUE);
            gc.fillText("❄ TIME FREEZE ACTIVE", WIDTH - 200, 20);
        }

        // Display active skill effects
        if (scoreFrenzyEnabled) {
            gc.setFill(Color.GOLD);
            gc.fillText("🔥 SCORE FRENZY: " + gameState.getComboMultiplier() + "x", WIDTH - 200, 40);
        }
    }

    // ===== SKILL SYSTEM METHODS =====

    /**
     * Ensure start button is visible and properly configured
     */
    private void ensureStartButtonVisible() {
        if (startButton != null) {
            startButton.setVisible(true);
            startButton.setManaged(true);
            startButton.setDisable(false);
            startButton.toFront();

            // Log button state for debugging
            System.out.println("Start button state - Visible: " + startButton.isVisible() +
                             ", Disabled: " + startButton.isDisabled() +
                             ", Text: " + startButton.getText());
        }
    }

    // Update skill progress display
    private void updateSkillProgress() {
        if (gameState != null) {
            double progress = gameState.getSkillProgress();
            int pointsToNext = gameState.getPointsToNextSkill();

            skillProgressBar.setProgress(progress);
            skillProgressLabel.setText(pointsToNext + "/" + GameState.getPointsPerSkill());
        }
    }

    // Reset all skill effects
    private void resetSkillEffects() {
        speedMultiplier = 1.0;
        scoreMultiplier = 1.0;
        foodMagnetRadius = 0;
        wallPhasingEnabled = false;
        ghostModeEnabled = false;
        doubleFood = false;
        basicShieldEnabled = false;
        steelBodyEnabled = false;
        secondChanceEnabled = false;
        invincibilityEnabled = false;
        timeFreezeEnabled = false;
        timeRewindEnabled = false;
        luckyStarEnabled = false;
        scoreFrenzyEnabled = false;
        worldShrinkEnabled = false;
        worldShrinkFactor = 1.0;
        shieldUses = 0;
        additionalFood.clear();
    }

    // Open skill menu
    private void openSkillMenu() {
        if (!gameRunning || skillMenuStage != null) return;

        // 暂停游戏
        gameRunning = false;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/skill-menu.fxml"));
            VBox skillMenuRoot = loader.load();
            SkillMenuController controller = loader.getController();

            skillMenuStage = new Stage();
            skillMenuStage.initModality(Modality.APPLICATION_MODAL);
            skillMenuStage.initStyle(StageStyle.UTILITY);
            skillMenuStage.setTitle("Skill Management");
            skillMenuStage.setScene(new Scene(skillMenuRoot));
            skillMenuStage.setResizable(false);

            controller.initializeController(gameState, skillManager, notificationSystem, () -> {
                skillMenuStage.close();
                skillMenuStage = null;
                // 恢复游戏
                gameRunning = true;
                Platform.runLater(() -> gameCanvas.requestFocus());
            });

            skillMenuStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load skill menu FXML");
        }
    }

    // Show skill notification
    private void showSkillNotification(SkillCard skill) {
        showSkillNotification("New Skill Unlocked: " + skill.getName());
    }

    /**
     * 显示技能详情界面
     */
    private void showSkillDetailScreen(SkillCard skill) {
        try {
            // 加载技能详情界面FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/skill-detail.fxml"));
            StackPane skillDetailRoot = loader.load();
            SkillDetailController controller = loader.getController();

            // 创建技能详情舞台
            skillDetailStage = new Stage();
            skillDetailStage.initModality(Modality.APPLICATION_MODAL);
            skillDetailStage.initOwner(primaryStage);
            skillDetailStage.initStyle(StageStyle.UNDECORATED);
            skillDetailStage.setTitle("技能详情");

            // 创建场景并添加CSS样式
            Scene skillDetailScene = new Scene(skillDetailRoot, WIDTH, HEIGHT);
            addStylesheet(skillDetailScene, "/css/skill-detail.css", "resources/css/skill-detail.css");
            addStylesheet(skillDetailScene, "/css/game-styles.css", "resources/css/game-styles.css");

            skillDetailScene.setFill(Color.TRANSPARENT);
            skillDetailStage.setScene(skillDetailScene);
            skillDetailStage.setResizable(false);

            // 初始化控制器
            controller.initializeController(
                skill,
                () -> {
                    // 选择技能回调
                    skillDetailStage.close();
                    skillDetailStage = null;

                    // 获得技能并恢复游戏
                    gameState.awardSkill(skill);
                    notificationSystem.showSkillUnlocked(skill);
                    gameRunning = true;

                    // 确保画布获得焦点
                    Platform.runLater(() -> gameCanvas.requestFocus());
                },
                () -> {
                    // 返回选择回调（这里实际上不会有其他选择，但保持接口一致性）
                    skillDetailStage.close();
                    skillDetailStage = null;

                    // 获得技能并恢复游戏
                    gameState.awardSkill(skill);
                    notificationSystem.showSkillUnlocked(skill);
                    gameRunning = true;

                    // 确保画布获得焦点
                    Platform.runLater(() -> gameCanvas.requestFocus());
                }
            );

            // 显示舞台
            skillDetailStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("无法加载技能详情界面: " + e.getMessage());

            // 回退处理：直接获得技能
            gameState.awardSkill(skill);
            notificationSystem.showSkillUnlocked(skill);
            gameRunning = true;
        }
    }

    private void showSkillNotification(String message) {
        // Create notification label
        Label notification = new Label(message);
        notification.setStyle("-fx-background-color: rgba(155, 89, 182, 0.9); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 10px 20px; " +
                            "-fx-background-radius: 10px;");

        // Position notification
        notification.setLayoutX(WIDTH / 2 - 100);
        notification.setLayoutY(50);

        // Add to scene (this is simplified - in real implementation you'd add to a proper container)
        System.out.println("Skill Notification: " + message);
    }

    private void showWinNotification() {
        System.out.println("🎉 CONGRATULATIONS! You've mastered all skills and won the game! 🎉");
    }

    /**
     * Show the game over screen with comprehensive statistics and options
     */
    private void showGameOverScreen(boolean isWinScreen) {
        if (gameOverStage != null && gameOverStage.isShowing()) {
            return; // Already showing
        }

        try {
            // Try multiple resource loading approaches
            URL fxmlResource = null;

            // First try: standard classpath resource
            fxmlResource = getClass().getResource("/fxml/game-over.fxml");

            // Second try: relative to class location
            if (fxmlResource == null) {
                fxmlResource = getClass().getResource("../../../resources/fxml/game-over.fxml");
            }

            // Third try: using ClassLoader
            if (fxmlResource == null) {
                fxmlResource = getClass().getClassLoader().getResource("fxml/game-over.fxml");
            }

            // Fourth try: direct file path approach
            if (fxmlResource == null) {
                try {
                    fxmlResource = new java.io.File("resources/fxml/game-over.fxml").toURI().toURL();
                } catch (Exception e) {
                    System.out.println("Could not load FXML as file: " + e.getMessage());
                }
            }

            if (fxmlResource == null) {
                System.out.println("ERROR: Could not find game-over.fxml resource");
                fallbackGameOver(isWinScreen);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource);
            StackPane gameOverRoot = loader.load();
            GameOverController controller = loader.getController();

            // Create the game over stage
            gameOverStage = new Stage();
            gameOverStage.initModality(Modality.APPLICATION_MODAL);
            gameOverStage.initOwner(primaryStage);
            gameOverStage.initStyle(StageStyle.UNDECORATED);
            gameOverStage.setTitle(isWinScreen ? "Victory!" : "Game Over");

            // Create scene with CSS styling
            Scene gameOverScene = new Scene(gameOverRoot, WIDTH, HEIGHT);

            // Add CSS stylesheets with robust resource loading
            addStylesheet(gameOverScene, "/css/game-over.css", "resources/css/game-over.css");
            addStylesheet(gameOverScene, "/css/game-styles.css", "resources/css/game-styles.css");

            gameOverScene.setFill(Color.TRANSPARENT);

            gameOverStage.setScene(gameOverScene);
            gameOverStage.setResizable(false);

            // Initialize controller with game data and callbacks
            controller.initializeController(
                gameState,
                skillManager,
                isWinScreen,
                maxSnakeLength,
                foodConsumed,
                this::restartGame,
                this::exitGame
            );

            // Show the stage
            gameOverStage.show();

            // Update main game UI to reflect game over state
            gameStatus.setText(isWinScreen ? "恭喜获胜! 最终分数: " + score : "游戏结束! 最终分数: " + score);
            startButton.setText("重新开始");
            startButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                               "-fx-padding: 10px 20px; -fx-border-radius: 5px; " +
                               "-fx-background-radius: 5px; -fx-cursor: hand; " +
                               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 1);");
            startButton.setDisable(false);
            skillMenuButton.setDisable(true);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load game over screen FXML");

            // Fallback to simple game over handling
            fallbackGameOver(isWinScreen);
        }
    }

    /**
     * Fallback game over handling if FXML loading fails
     */
    private void fallbackGameOver(boolean isWinScreen) {
        // Create a simple programmatic game over screen
        try {
            Stage fallbackStage = new Stage();
            fallbackStage.initModality(Modality.APPLICATION_MODAL);
            fallbackStage.initOwner(primaryStage);
            fallbackStage.setTitle(isWinScreen ? "胜利！" : "游戏结束");

            VBox container = new VBox(20);
            container.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(40));
            container.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");

            // Title
            Label titleLabel = new Label(isWinScreen ? "胜利！" : "游戏结束");
            titleLabel.setStyle("-fx-text-fill: " + (isWinScreen ? "#2ecc71" : "#e74c3c") +
                               "; -fx-font-size: 48px; -fx-font-weight: bold;");

            // Score
            Label scoreLabel = new Label("最终得分: " + score);
            scoreLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 32px; -fx-font-weight: bold;");

            // Statistics
            VBox statsBox = new VBox(10);
            statsBox.setAlignment(Pos.CENTER);
            statsBox.setStyle("-fx-background-color: #34495e; -fx-padding: 20px; -fx-border-radius: 10px;");

            Label statsTitle = new Label("游戏统计");
            statsTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

            Label timeLabel = new Label("游戏时长: " + formatTime(gameState.getGameDuration()));
            timeLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");

            Label skillsLabel = new Label("获得技能: " + gameState.getSkillsEarned());
            skillsLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");

            Label foodLabel = new Label("食物消耗: " + foodConsumed);
            foodLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");

            Label lengthLabel = new Label("最大长度: " + maxSnakeLength);
            lengthLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");

            statsBox.getChildren().addAll(statsTitle, timeLabel, skillsLabel, foodLabel, lengthLabel);

            // Buttons
            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);

            Button restartBtn = new Button("🔄 再玩一次");
            restartBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                               "-fx-font-size: 16px; -fx-padding: 10px 20px; -fx-border-radius: 5px;");
            restartBtn.setOnAction(e -> {
                fallbackStage.close();
                restartGame();
            });

            Button exitBtn = new Button("🚪 退出游戏");
            exitBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                            "-fx-font-size: 16px; -fx-padding: 10px 20px; -fx-border-radius: 5px;");
            exitBtn.setOnAction(e -> {
                fallbackStage.close();
                exitGame();
            });

            buttonBox.getChildren().addAll(restartBtn, exitBtn);

            container.getChildren().addAll(titleLabel, scoreLabel, statsBox, buttonBox);

            Scene fallbackScene = new Scene(container, 400, 500);
            fallbackStage.setScene(fallbackScene);
            fallbackStage.setResizable(false);
            fallbackStage.show();

            // Store reference for cleanup
            gameOverStage = fallbackStage;

        } catch (Exception e) {
            System.out.println("Error creating fallback game over screen: " + e.getMessage());
            // Ultimate fallback - just update the main UI
            gameStatus.setText(isWinScreen ? "恭喜获胜! 最终分数: " + score : "游戏结束! 最终分数: " + score);
            startButton.setText("重新开始");
            startButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                               "-fx-padding: 10px 20px; -fx-border-radius: 5px; " +
                               "-fx-background-radius: 5px; -fx-cursor: hand; " +
                               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 2, 0, 0, 1);");
            startButton.setDisable(false);
            skillMenuButton.setDisable(true);

            Platform.runLater(() -> {
                startButton.requestFocus();
                startButton.setVisible(true);
            });
        }
    }

    /**
     * Format time duration in MM:SS format
     */
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    /**
     * Restart the game (callback from game over screen)
     */
    private void restartGame() {
        if (gameOverStage != null && gameOverStage.isShowing()) {
            gameOverStage.close();
            gameOverStage = null;
        }

        // Restart the game
        initializeGame();
        startGame();
    }

    /**
     * Exit the game (callback from game over screen)
     */
    private void exitGame() {
        if (gameOverStage != null && gameOverStage.isShowing()) {
            gameOverStage.close();
            gameOverStage = null;
        }

        // Close the application
        Platform.exit();
        System.exit(0);
    }

    /**
     * Helper method to add stylesheets with robust resource loading
     */
    private void addStylesheet(Scene scene, String classpathResource, String fileResource) {
        try {
            URL cssResource = null;

            // Try classpath first
            cssResource = getClass().getResource(classpathResource);

            // Try ClassLoader
            if (cssResource == null) {
                cssResource = getClass().getClassLoader().getResource(classpathResource.substring(1));
            }

            // Try direct file path
            if (cssResource == null) {
                try {
                    cssResource = new java.io.File(fileResource).toURI().toURL();
                } catch (Exception e) {
                    System.out.println("Could not load CSS as file: " + fileResource);
                }
            }

            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
                System.out.println("Successfully loaded CSS: " + classpathResource);
            } else {
                System.out.println("Warning: Could not load CSS resource: " + classpathResource);
            }
        } catch (Exception e) {
            System.out.println("Error loading CSS " + classpathResource + ": " + e.getMessage());
        }
    }

    // Generate additional food for double food skill
    private void generateAdditionalFood() {
        Random random = new Random();
        int x, y;
        boolean validPosition;

        do {
            validPosition = true;
            x = random.nextInt(GRID_WIDTH);
            y = random.nextInt(GRID_HEIGHT);

            // Check if position conflicts with snake
            for (Point p : snake) {
                if (p.x == x && p.y == y) {
                    validPosition = false;
                    break;
                }
            }

            // Check if position conflicts with main food
            if (food.x == x && food.y == y) {
                validPosition = false;
            }

        } while (!validPosition);

        additionalFood.add(new Point(x, y));
    }

    // Apply food magnet effect
    private Point applyFoodMagnet(Point newHead) {
        // Check if food is within magnet radius
        int dx = food.x - newHead.x;
        int dy = food.y - newHead.y;
        int distance = Math.abs(dx) + Math.abs(dy); // Manhattan distance

        if (distance <= foodMagnetRadius) {
            // Move towards food
            if (Math.abs(dx) > Math.abs(dy)) {
                newHead.x += (dx > 0) ? 1 : -1;
            } else if (dy != 0) {
                newHead.y += (dy > 0) ? 1 : -1;
            }
        }

        return newHead;
    }

    // ===== COMPREHENSIVE SKILL EFFECTS IMPLEMENTATION =====

    // Movement effects
    @Override
    public void applySpeedMultiplier(double multiplier) {
        this.speedMultiplier = multiplier;
    }

    @Override
    public void enableWallPhasing() {
        this.wallPhasingEnabled = true;
    }

    // Food effects
    @Override
    public void enableFoodMagnet(int radius) {
        this.foodMagnetRadius = radius;
    }

    @Override
    public void enableDoubleFood() {
        this.doubleFood = true;
        if (additionalFood.isEmpty()) {
            generateAdditionalFood();
        }
    }

    @Override
    public void enableLuckyStarFood() {
        this.luckyStarEnabled = true;
    }

    // Scoring effects
    @Override
    public void applyScoreMultiplier(double multiplier) {
        this.scoreMultiplier = multiplier;
    }

    @Override
    public void enableScoreFrenzy() {
        this.scoreFrenzyEnabled = true;
        gameState.enableScoreFrenzy();
    }

    // Survival effects
    @Override
    public void enableBasicShield() {
        this.basicShieldEnabled = true;
        this.shieldUses = 1; // Single use
    }

    @Override
    public void enableSteelBody() {
        this.steelBodyEnabled = true;
    }

    @Override
    public void enableGhostMode() {
        this.ghostModeEnabled = true;
    }

    @Override
    public void enableSecondChance() {
        this.secondChanceEnabled = true;
    }

    @Override
    public void enableInvincibility() {
        this.invincibilityEnabled = true;
    }

    // Time effects
    @Override
    public void enableTimeRewind() {
        this.timeRewindEnabled = true;
    }

    @Override
    public void enableTimeFreezeMode() {
        this.timeFreezeEnabled = true;
    }

    // World effects
    @Override
    public void enableWorldShrink() {
        this.worldShrinkEnabled = true;
        this.worldShrinkFactor = 0.7; // 30% shrink
    }

    // System effects
    @Override
    public void expandSkillSlots() {
        // This would be handled by GameState
        notificationSystem.showGameNotification("Skill Slots", "Additional slot unlocked!", "📦");
    }

    @Override
    public void teleportSnake() {
        if (snake.isEmpty()) return;

        Random random = new Random();
        Point newPosition;
        boolean validPosition;

        do {
            validPosition = true;
            newPosition = new Point(
                random.nextInt(GRID_WIDTH),
                random.nextInt(GRID_HEIGHT)
            );

            // Check if position is safe (not on food or snake body)
            if (newPosition.x == food.x && newPosition.y == food.y) {
                validPosition = false;
            }

            for (Point p : snake) {
                if (p.x == newPosition.x && p.y == newPosition.y) {
                    validPosition = false;
                    break;
                }
            }

        } while (!validPosition);

        // Move snake head to new position
        snake.removeFirst();
        snake.addFirst(newPosition);
    }

    // Size effects
    @Override
    public void shrinkSnake() {
        // Remove half of the snake's length (minimum 1 segment)
        int segmentsToRemove = Math.max(1, snake.size() / 2);

        for (int i = 0; i < segmentsToRemove && snake.size() > 1; i++) {
            snake.removeLast();
        }

        notificationSystem.showGameNotification("Body Split", "Snake length halved!", "✂");
    }

    @Override
    public void growSnake() {
        // Double the snake's length by duplicating tail segments
        int currentSize = snake.size();
        Point tail = snake.getLast();

        for (int i = 0; i < currentSize && i < 10; i++) { // Limit growth to prevent excessive length
            snake.addLast(new Point(tail.x, tail.y));
        }

        notificationSystem.showGameNotification("Body Growth", "Snake length doubled!", "📈");
    }

    /**
     * Save current game state for time rewind
     */
    private void saveGameStateSnapshot() {
        // Create snapshot data containing snake position, food position, etc.
        GameStateData snapshotData = new GameStateData(
            new ArrayDeque<>(snake),
            new Point(food.x, food.y),
            new ArrayList<>(additionalFood),
            currentDirection
        );

        gameState.saveStateSnapshot(snapshotData);
    }

    /**
     * Apply world shrink effect to game area
     */
    private void applyWorldShrinkEffect() {
        if (worldShrinkEnabled) {
            // This would modify the effective game area
            // For now, we'll just show a notification
            notificationSystem.showGameNotification("World Shrink", "Game area reduced!", "🔄");
        }
    }

    /**
     * Game state data for snapshots
     */
    private static class GameStateData {
        final Deque<Point> snakePosition;
        final Point foodPosition;
        final List<Point> additionalFoodPositions;
        final Direction direction;

        GameStateData(Deque<Point> snakePosition, Point foodPosition,
                     List<Point> additionalFoodPositions, Direction direction) {
            this.snakePosition = snakePosition;
            this.foodPosition = foodPosition;
            this.additionalFoodPositions = additionalFoodPositions;
            this.direction = direction;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

