import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    int boardWidth;
    int boardHeight;
    int tileSize = 25;

    // Snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    // Food
    Tile food;
    Random random;

    // Game logic
    Timer gameLoop;
    int velocityX;
    int velocityY;
    boolean gameOver = false;

    // Restart button
    JButton restartButton;

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        // Initialize snake and food
        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();
        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        velocityX = 0;
        velocityY = 0;

        // Timer for game loop
        gameLoop = new Timer(100, this);
        gameLoop.start();

        // Create Restart button (hidden initially)
        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 20));
        restartButton.setBounds(boardWidth / 2 - 60, boardHeight / 2 + 50, 120, 40);
        restartButton.setFocusable(false);
        restartButton.setVisible(false);

        // Add cursor change on hover
        restartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Set button background and text colors
        restartButton.setBackground(Color.LIGHT_GRAY);
        restartButton.setForeground(Color.BLACK);

        // Add hover effect using mouse listeners
        restartButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                restartButton.setBackground(Color.green); // Change background on hover
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                restartButton.setBackground(Color.LIGHT_GRAY); // Reset background when not hovered
            }
        });

        // Add action listener for restart
        restartButton.addActionListener(e -> restartGame());
        setLayout(null); // Use null layout for precise positioning
        add(restartButton);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // if the game is not over
        if (!gameOver) {
            // Food
            g.setColor(Color.red);
            g.fill3DRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, true);

            // Snake Head
            g.setColor(Color.green);
            g.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, true);

            // Snake Body
            for (Tile snakePart : snakeBody) {
                g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
            }

            // Score
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.setColor(Color.white);
            g.drawString("Score: " + snakeBody.size(), tileSize - 16, tileSize);
        } else {
            // Blur effect
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2d.setColor(Color.black);
            g2d.fillRect(0, 0, boardWidth, boardHeight);
            g2d.dispose();

            // Game Over message
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(Color.red);
            String gameOverText = "GAME OVER";
            FontMetrics metrics = g.getFontMetrics();
            int x = (boardWidth - metrics.stringWidth(gameOverText)) / 2;
            // Position the Game Over text slightly above the middle
            int y = (boardHeight / 2) - 50; 
            g.drawString(gameOverText, x, y);

            // Final Score
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(Color.WHITE);
            String scoreText = "Final Score: " + snakeBody.size();
            int scoreX = (boardWidth - g.getFontMetrics().stringWidth(scoreText)) / 2;
            // Position the Final Score text below the Game Over text but above the button
            int scoreY = y + metrics.getHeight(); 
            g.drawString(scoreText, scoreX, scoreY);

            // Show Restart button
            restartButton.setVisible(true);
            // Position Restart button below the score
            restartButton.setBounds(boardWidth / 2 - 60, scoreY + 40, 120, 40);
        }
    }

    public void placeFood() {
        food.x = random.nextInt(boardWidth / tileSize);
        food.y = random.nextInt(boardHeight / tileSize);
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    public void move() {
        // Eat food
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
        }

        // Snake Body
        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) {
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i - 1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        // Snake Head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        // Game over conditions
        for (Tile snakePart : snakeBody) {
            // collide with the snake body
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }
        // collide with Frame Ends
        if (snakeHead.x * tileSize < 0 || snakeHead.x * tileSize >= boardWidth ||
            snakeHead.y * tileSize < 0 || snakeHead.y * tileSize >= boardHeight) {
            gameOver = true;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
        }
        repaint(); // it basically calls draw over and over again
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
    }

    // do not need
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public void restartGame() {
        // Reset game state
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        placeFood();
        velocityX = 0;
        velocityY = 0;
        gameOver = false;
        restartButton.setVisible(false);
        gameLoop.start();
        repaint();
    }
}
