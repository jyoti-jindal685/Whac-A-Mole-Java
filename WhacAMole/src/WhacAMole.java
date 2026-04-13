import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

public class WhacAMole {
    int boardWidth = 640;
    int boardHeight = 720;

    JFrame frame = new JFrame("Mario: Whac A Mole");
    JLabel scoreLabel = new JLabel();
    JLabel timeLabel = new JLabel();
    JLabel levelLabel = new JLabel();
    JLabel highScoreLabel = new JLabel();
    JLabel statusLabel = new JLabel();

    JPanel topPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel bottomPanel = new JPanel();

    JButton[] board = new JButton[9];
    JButton restartButton = new JButton("Restart Game");

    ImageIcon moleIcon;
    ImageIcon plantIcon;

    JButton currMoleTile;
    JButton currPlantTile;

    Random random = new Random();
    Timer setMoleTimer;
    Timer setPlantTimer;
    Timer countdownTimer;

    int score = 0;
    int highScore = 0;
    int timeRemaining = 30;
    int level = 1;
    boolean gameOver = false;

    WhacAMole() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.getContentPane().setBackground(new Color(225, 240, 255));

        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        levelLabel.setFont(new Font("Arial", Font.BOLD, 24));
        highScoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 28));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(240, 255, 245));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.setLayout(new GridLayout(2, 1, 8, 8));
        topPanel.setOpaque(false);
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statsPanel.setOpaque(false);
        statsPanel.add(scoreLabel);
        statsPanel.add(timeLabel);
        statsPanel.add(levelLabel);
        statsPanel.add(highScoreLabel);
        topPanel.add(statsPanel);
        topPanel.add(statusLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(3, 3, 10, 10));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        boardPanel.setBackground(new Color(40, 110, 185));
        frame.add(boardPanel, BorderLayout.CENTER);

        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 12));
        bottomPanel.setOpaque(false);
        restartButton.setFont(new Font("Arial", Font.BOLD, 18));
        restartButton.setBackground(new Color(255, 210, 60));
        restartButton.setFocusable(false);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> resetGame());
        bottomPanel.add(restartButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        Image plantImg = new ImageIcon(getClass().getResource("./piranha.jpg")).getImage();
        plantIcon = new ImageIcon(plantImg.getScaledInstance(160, 140, Image.SCALE_SMOOTH));

        Image moleImg = new ImageIcon(getClass().getResource("./monty.jpg")).getImage();
        moleIcon = new ImageIcon(moleImg.getScaledInstance(160, 170, Image.SCALE_SMOOTH));

        loadHighScore();
        initializeBoard();
        resetGame();
        frame.setVisible(true);
    }

    private void initializeBoard() {
        for (int i = 0; i < 9; i++) {
            JButton tile = new JButton();
            tile.setFocusable(false);
            tile.setBackground(Color.white);
            tile.setOpaque(true);
            tile.setBorder(BorderFactory.createLineBorder(Color.gray, 2));
            tile.addActionListener(e -> handleTileClick((JButton) e.getSource()));
            board[i] = tile;
            boardPanel.add(tile);
        }
    }

    private void resetGame() {
        score = 0;
        timeRemaining = 30;
        level = 1;
        gameOver = false;
        currMoleTile = null;
        currPlantTile = null;
        restartButton.setVisible(false);
        statusLabel.setText("Tap the mole fast! Avoid the plant.");

        for (int i = 0; i < 9; i++) {
            board[i].setIcon(null);
            board[i].setEnabled(true);
        }

        if (setMoleTimer == null) {
            setMoleTimer = new Timer(getMoleDelayForLevel(level), e -> moveMole());
        }
        if (setPlantTimer == null) {
            setPlantTimer = new Timer(getPlantDelayForLevel(level), e -> movePlant());
        }
        if (countdownTimer == null) {
            countdownTimer = new Timer(1000, e -> tickCountdown());
        }

        setMoleTimer.setDelay(getMoleDelayForLevel(level));
        setPlantTimer.setDelay(getPlantDelayForLevel(level));
        updateLabels();

        setMoleTimer.start();
        setPlantTimer.start();
        countdownTimer.start();
    }

    private void handleTileClick(JButton tile) {
        if (gameOver) return;
        if (tile == currMoleTile) {
            score += 10;
            updateLevelIfNeeded();
            updateLabels();
        } else if (tile == currPlantTile) {
            gameOver("Ouch! You hit the plant.");
        }
    }

    private void moveMole() {
        if (gameOver) return;
        if (currMoleTile != null) {
            currMoleTile.setIcon(null);
            currMoleTile = null;
        }
        int num = random.nextInt(9);
        JButton tile = board[num];
        if (currPlantTile == tile) return;
        currMoleTile = tile;
        currMoleTile.setIcon(moleIcon);
    }

    private void movePlant() {
        if (gameOver) return;
        if (currPlantTile != null) {
            currPlantTile.setIcon(null);
            currPlantTile = null;
        }
        int num = random.nextInt(9);
        JButton tile = board[num];
        if (currMoleTile == tile) return;
        currPlantTile = tile;
        currPlantTile.setIcon(plantIcon);
    }

    private void tickCountdown() {
        if (gameOver) return;
        timeRemaining--;
        updateLabels();
        if (timeRemaining <= 0) {
            gameOver("Time's up!");
        }
    }

    private void updateLevelIfNeeded() {
        int newLevel = 1;
        if (score >= 120) newLevel = 4;
        else if (score >= 80) newLevel = 3;
        else if (score >= 40) newLevel = 2;

        if (newLevel != level) {
            level = newLevel;
            setMoleTimer.setDelay(getMoleDelayForLevel(level));
            setPlantTimer.setDelay(getPlantDelayForLevel(level));
            statusLabel.setText("Level " + level + " reached! Mole speed increased.");
        }
    }

    private int getMoleDelayForLevel(int level) {
        switch (level) {
            case 4:
                return 450;
            case 3:
                return 600;
            case 2:
                return 750;
            default:
                return 1000;
        }
    }

    private int getPlantDelayForLevel(int level) {
        switch (level) {
            case 4:
                return 900;
            case 3:
                return 1100;
            case 2:
                return 1300;
            default:
                return 1500;
        }
    }

    private void updateLabels() {
        scoreLabel.setText("Score: " + score);
        timeLabel.setText("Time: " + timeRemaining + "s");
        levelLabel.setText("Level: " + level);
        highScoreLabel.setText("High Score: " + highScore);
    }

    private void gameOver(String message) {
        gameOver = true;
        setMoleTimer.stop();
        setPlantTimer.stop();
        countdownTimer.stop();

        for (int i = 0; i < 9; i++) {
            board[i].setEnabled(false);
        }

        if (score > highScore) {
            highScore = score;
            saveHighScore();
            statusLabel.setText(message + " New high score: " + highScore + "!");
        } else {
            statusLabel.setText(message + " Final score: " + score);
        }

        updateLabels();
        restartButton.setVisible(true);
    }

    private void loadHighScore() {
        try {
            File file = new File("WhacAMoleHighScore.txt");
            if (file.exists()) {
                Scanner sc = new Scanner(file);
                if (sc.hasNextInt()) {
                    highScore = sc.nextInt();
                }
                sc.close();
            }
        } catch (Exception ignored) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (PrintWriter out = new PrintWriter(new FileWriter("WhacAMoleHighScore.txt"))) {
            out.println(highScore);
        } catch (IOException ignored) {
        }
    }
}
