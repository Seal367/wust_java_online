package com.example.javafx3;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGameWSAD extends Application {

    // 游戏常量
    private static final int WIDTH = 600;
    private static final int HEIGHT = 500;
    private static final int CELL_SIZE = 20;
    private static final int GRID_WIDTH = WIDTH / CELL_SIZE;
    private static final int GRID_HEIGHT = (HEIGHT - 100) / CELL_SIZE;
    private static final int INITIAL_SPEED = 150; // 毫秒

    // 游戏变量
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction currentDirection = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private Deque<Point> snake = new ArrayDeque<>();
    private Point food;
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
    private Label focusLabel; // 焦点状态标签

    @Override
    public void start(Stage primaryStage) {
        // 创建主布局
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        // 创建游戏画布
        gameCanvas = new Canvas(WIDTH, HEIGHT - 100);
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

        // 键盘事件处理 - 只监听WSAD键
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W ||
                    e.getCode() == KeyCode.S ||
                    e.getCode() == KeyCode.A ||
                    e.getCode() == KeyCode.D) {
                handleKeyPress(e.getCode());
                e.consume(); // 阻止事件继续传播
            }
        });

        // 初始化游戏
        initializeGame();

        // 设置舞台
        primaryStage.setTitle("JavaFX 贪吃蛇游戏 (WSAD控制)");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // 启动游戏循环
        startGameLoop();
    }

    // 创建控制面板
    private HBox createControlPanel() {
        HBox panel = new HBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(15));
        panel.setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        startButton = new Button("开始游戏");
        startButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        startButton.setOnMouseClicked(e -> startGame());

        Button exitButton = new Button("退出游戏");
        exitButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        exitButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        exitButton.setOnAction(e -> System.exit(0));

        // 添加控制说明
        Label controlLabel = new Label("控制: W-上  S-下  A-左  D-右");
        controlLabel.setTextFill(Color.LIGHTGRAY);
        controlLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        panel.getChildren().addAll(startButton, exitButton, controlLabel);
        return panel;
    }

    // 创建状态面板
    private HBox createStatusPanel() {
        HBox panel = new HBox(30);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(15));
        panel.setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        scoreLabel = new Label("分数: 0");
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        gameStatus = new Label("按开始按钮开始游戏");
        gameStatus.setTextFill(Color.GOLD);
        gameStatus.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // 焦点状态显示
        focusLabel = new Label("焦点: 未获得");
        focusLabel.setTextFill(Color.CYAN);
        focusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // 兼容旧版本的焦点绑定
        gameCanvas.focusedProperty().addListener((obs, oldVal, newVal) -> {
            focusLabel.setText(newVal ? "焦点: 已获得" : "焦点: 未获得");
        });

        panel.getChildren().addAll(scoreLabel, gameStatus, focusLabel);
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

        // 更新UI
        updateScore();
        gameStatus.setText("按开始按钮开始游戏");
        startButton.setText("开始游戏");
        startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

        // 绘制初始状态
        drawGame();

        // 确保按钮获得焦点
        Platform.runLater(() -> startButton.requestFocus());
    }

    // 开始游戏
    private void startGame() {
        if (gameOver) {
            initializeGame();
        }
        gameRunning = true;
        gameOver = false;
        gameStatus.setText("游戏进行中...");
        startButton.setText("重新开始");
        startButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

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

        // 处理方向缓冲区
        if (!directionBuffer.isEmpty()) {
            nextDirection = directionBuffer.pollFirst();
        }

        // 应用方向变化
        currentDirection = nextDirection;

        // 计算蛇头的新位置
        Point head = snake.getFirst();
        Point newHead = new Point(head.x, head.y);

        switch (currentDirection) {
            case UP: newHead.y--; break;
            case DOWN: newHead.y++; break;
            case LEFT: newHead.x--; break;
            case RIGHT: newHead.x++; break;
        }

        // 检查是否撞墙
        if (newHead.x < 0 || newHead.x >= GRID_WIDTH ||
                newHead.y < 0 || newHead.y >= GRID_HEIGHT) {
            gameOver();
            return;
        }

        // 检查是否撞到自己
        for (Point p : snake) {
            if (p.x == newHead.x && p.y == newHead.y) {
                gameOver();
                return;
            }
        }

        // 移动蛇
        snake.addFirst(newHead);

        // 检查是否吃到食物
        if (newHead.x == food.x && newHead.y == food.y) {
            // 吃到食物，增加分数
            score += 10;
            updateScore();

            // 生成新食物
            generateFood();

            // 增加速度
            if (speed > 50) speed -= 2;
        } else {
            // 没吃到食物，移除尾部
            snake.removeLast();
        }

        // 绘制游戏
        drawGame();
    }

    // 游戏结束
    private void gameOver() {
        gameOver = true;
        gameRunning = false;
        gameStatus.setText("游戏结束! 最终分数: " + score);
        startButton.setText("重新开始");
        startButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        // 焦点回到按钮
        Platform.runLater(() -> startButton.requestFocus());
    }

    // 更新分数显示
    private void updateScore() {
        scoreLabel.setText("分数: " + score);
    }

    // 绘制游戏
    private void drawGame() {
        // 清除画布
        gc.clearRect(0, 0, WIDTH, HEIGHT - 100);

        // 绘制网格背景
        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRect(0, 0, WIDTH, HEIGHT - 100);

        // 绘制网格线
        gc.setStroke(Color.rgb(50, 50, 50));
        gc.setLineWidth(0.5);
        for (int x = 0; x <= WIDTH; x += CELL_SIZE) {
            gc.strokeLine(x, 0, x, HEIGHT - 100);
        }
        for (int y = 0; y <= HEIGHT - 100; y += CELL_SIZE) {
            gc.strokeLine(0, y, WIDTH, y);
        }

        // 绘制食物
        gc.setFill(Color.RED);
        gc.fillOval(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeOval(food.x * CELL_SIZE, food.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // 绘制蛇
        int index = 0;
        for (Point p : snake) {
            // 蛇头
            if (index == 0) {
                gc.setFill(Color.GREEN);
            }
            // 蛇身
            else {
                // 创建渐变效果
                double factor = 0.7 + (0.3 * index / snake.size());
                gc.setFill(Color.rgb(0, (int)(255 * factor), 0));
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

        // 绘制方向缓冲区状态
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

        // 游戏结束提示
        if (gameOver) {
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRect(0, 0, WIDTH, HEIGHT - 100);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            gc.fillText("游戏结束", WIDTH/2 - 100, (HEIGHT - 100)/2 - 30);

            gc.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            gc.fillText("分数: " + score, WIDTH/2 - 70, (HEIGHT - 100)/2 + 30);
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

                // 控制游戏更新速度
                if (now - lastUpdate >= speed * 1_000_000) {
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

    public static void main(String[] args) {
        launch(args);
    }
}