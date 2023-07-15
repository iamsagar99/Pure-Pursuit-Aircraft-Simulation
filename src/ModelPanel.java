import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModelPanel extends JPanel {
    static Dimension size= Toolkit.getDefaultToolkit().getScreenSize();

    static final int SCREEN_WIDTH = (int)size.getWidth() - 100;
    static final int SCREEN_HEIGHT = (int)size.getHeight() -100;

    static final int DELAY = 80;

    static final int CITY_BASEX = 0;
    static final int CITY_BASEY = 600;

    private double fighterX;
    private double fighterY;

    private double bomberX;
    private double bomberY;
    private double bomberSpeed;
    private double previousBomberSpeed;

    private boolean bomberDestroyed;
    private List<Rectangle> cityRectangles;

    private List<Bullet> bullets;
    private List<Bomb> bombs;
    private List<Rectangle> destroyedRectangles;
    private int destroyedTimer;

    Timer timer;
    int timerCount;

    static final int VF = 5; // Fighter's speed

    ModelPanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.lightGray);
        this.setFocusable(true);
        fighterX = 100;
        fighterY = 100;
        bomberX = 200;
        bomberY = 200;

        bomberDestroyed = false;
        cityRectangles = generateCityRectangles();

        bullets = new ArrayList<>();
        bombs = new ArrayList<>();
        destroyedRectangles = new ArrayList<>();
        destroyedTimer = 0;

        timerCount = 0;
        startSimulation();
    }

    public void startSimulation() {
        timer = new Timer(DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timerCount++;

                if (timerCount % 4 == 0) {
                    previousBomberSpeed = bomberSpeed;
                    bomberSpeed = generateRandomSpeed(VF - 3, VF + 3);

                    // Release bullet to hit the bomber
                    if (bomberSpeed != previousBomberSpeed) {
                        bullets.add(new Bullet((int) fighterX + 35, (int) fighterY + 60, calculateBulletAngle()));
                    }
                }

                // Calculate the distance between the fighter and the bomber
                double distance = Math.abs(bomberX - fighterX);

                // Check if the bomber is destroyed by the bullet
                List<Bullet> bulletsToRemove = new ArrayList<>();
                for (Bullet bullet : bullets) {
                    bullet.move();
                    if (bullet.intersects(new Rectangle((int) bomberX, (int) bomberY, 70, 25))) {
                        bulletsToRemove.add(bullet);
                        bomberDestroyed = true;
                        break;
                    }
                }
                bullets.removeAll(bulletsToRemove);

                // Update the fighter's position based on its velocity
                fighterX += VF;

                // Update the bomber's position based on its speed
                bomberX += bomberSpeed;
                bomberY += (bomberSpeed - 10)* Math.sin(bomberX + bomberY); // Move in a curved path

                // Check if the bomber escapes
                if (timer.getDelay() >= 12 * DELAY) {
                    System.out.println("Bomber escaped!");
                    timer.stop();
                }

                // Check if the bomber drops the bomb
                if (timerCount % 10 == 0) {
                    dropBomb();
                }

                // Check if the bomb hits any city rectangles
                List<Rectangle> rectanglesToRemove = new ArrayList<>();
                for (Bomb bomb : bombs) {
                    bomb.move();
                    if (bomb.getY() >= CITY_BASEY) {
                        for (Rectangle rectangle : cityRectangles) {
                            if (bomb.intersects(rectangle)) {
                                rectanglesToRemove.add(rectangle);
                                destroyedRectangles.addAll(rectanglesToRemove);
                                destroyedTimer = 3;
                                break;
                            }
                        }
                    }
                }
                cityRectangles.removeAll(rectanglesToRemove);

                // Repaint the panel to update the positions
                if (bomberY != CITY_BASEY) {
                    repaint();
                }

            }
        });

        timer.start();
    }

    private void dropBomb() {
        // Update the position of the bomb based on the bomber's position
        int bombX = (int) bomberX + 50;
        int bombY = (int) bomberY;
        bombs.add(new Bomb(bombX, bombY));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawFighterPlane(g);
        drawVillainPlane(g);

        if (bomberDestroyed) {
            drawFallingBomber(g);
        }

        drawBullets(g);
        drawBombs(g);
        drawCity(g);

        if (destroyedTimer > 0) {
            drawDestroyedCityRectangles(g);
            destroyedTimer--;
        }
    }

    private void drawFallingBomber(Graphics g) {
        int bomberWidth = 70;
        int bomberHeight = 25;

        int currentPositionY = (int) bomberY;
        for (double i = currentPositionY; i <= CITY_BASEY; i += 0.000001) {
            bomberY = bomberY + i;
        }

        g.setColor(Color.RED);
        g.fillRect((int) bomberX, (int) bomberY, bomberWidth, bomberHeight);
    }

    public void drawFighterPlane(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect((int) fighterX, (int) fighterY, 70, 30); // Filled rectangle

//        g.setColor(Color.GREEN);
        g.fillOval((int) fighterX + 35, (int) fighterY, 70, 30); // Filled oval

//        g.setColor(Color.GREEN);
        g.fillOval((int) fighterX - 25, (int) fighterY, 50, 30); // Filled oval

//        g.setColor(Color.GREEN);
        g.fillOval((int) fighterX - 27, (int) fighterY - 15, 20, 40);
    }

    public void drawVillainPlane(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval((int) bomberX, (int) bomberY, 70, 25);
        g.fillOval((int) bomberX, (int) bomberY - 10, 15, 30);
    }

    public void drawBullets(Graphics g) {
        g.setColor(Color.BLUE);
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
    }

    public void drawBombs(Graphics g) {
        g.setColor(Color.ORANGE);
        for (Bomb bomb : bombs) {
            bomb.draw(g);
        }
    }

    public void drawCity(Graphics g) {
        g.setColor(Color.BLACK);
        g.drawLine(CITY_BASEX, CITY_BASEY, SCREEN_WIDTH, CITY_BASEY);

        g.setColor(Color.GRAY);
        for (Rectangle rectangle : cityRectangles) {
            g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
    }

    public void drawDestroyedCityRectangles(Graphics g) {
        g.setColor(Color.RED);
        for (Rectangle rectangle : destroyedRectangles) {
            g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }
    }

    private List<Rectangle> generateCityRectangles() {
        List<Rectangle> rectangles = new ArrayList<>();
        Random random = new Random();

        int sum = 0;
        while (sum < SCREEN_WIDTH) {
            int width = random.nextInt(10) + 30;
            int height = random.nextInt(70) + 30;

            rectangles.add(new Rectangle(sum, CITY_BASEY - height, width, height));
            sum += width;
        }

        return rectangles;
    }

    private int generateRandomSpeed(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    private double calculateBulletAngle() {
        double distance = Math.abs(bomberX - fighterX);
        double sinTheta = (bomberY - fighterY) / distance;
        double cosTheta = (bomberX - fighterX) / distance;

        return Math.atan2(sinTheta, cosTheta);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Fighter Aircraft Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        ModelPanel modelPanel = new ModelPanel();
        frame.getContentPane().add(modelPanel);

        frame.pack();
        frame.setVisible(true);
    }
}

class Bullet extends Rectangle {
    private static final int BULLET_SPEED = 10;
    private double angle;

    Bullet(int x, int y, double angle) {
        super(x, y, 20, 30);
        this.angle = angle;
    }

    void move() {
        x += BULLET_SPEED * Math.cos(angle);
        y += BULLET_SPEED * Math.sin(angle);
    }

    void draw(Graphics g) {
        g.fillOval(x, y, width, height);
    }
}

class Bomb extends Rectangle {
    private static final int BOMB_SPEED = 5;

    Bomb(int x, int y) {
        super(x, y, 20, 20);
    }

    void move() {
        y += BOMB_SPEED; // Move downward
    }

    void draw(Graphics g) {
        g.fillOval(x, y, width, height);
    }
}
