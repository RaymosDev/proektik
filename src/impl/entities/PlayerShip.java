package impl.entities;

import gameEngine.Entity;
import gameEngine.Game;
import gameEngine.InputManager;
import gameEngine.ResourceLoader; 
import gameEngine.Scene;
import gameEngine.SceneObject;
import gameEngine.Vector2;
import impl.Main;
import impl.ResolutionConfig;
import impl.scenes.GameScene; 
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image; 
import java.awt.event.KeyEvent; 

/**
 *  Корабль игрока, ничего больше
 */

public class PlayerShip extends Entity implements DamagableEntity {
    private static int WIDTH; 
    private static int HEIGHT; 
    private static double LASER_COOLDOWN = 0.22; // Задержка перед след. возможным выстрелом лазера
    private static final double SPEED = 600; 
    private static final double MAX_HEALTH = 15; 

    private static  double LASER_DAMAGE_AMOUNT = 1; // Урон от лазера.
    private static  double LASER_SPEED = 1500; // Скорость лазера.
    private static int LASER_WIDTH; 
    private static int LASER_HEIGHT;

    public static Image PLAYER_1; 
    public static Image PLAYER_2; 
    public static Image PLAYER_LASER; 

    private Image sprite1; 
    private Image sprite2; 
    private double currentHealth;
    private double nextFireTime; // Время следующего выстрела

    private boolean superLaserOn = false;

    static {
        updateDimensions();
    }


    public PlayerShip(Vector2 position) {
        super(position, new Vector2(WIDTH, HEIGHT)); // Вызов конструктора Entity
        sprite1 = PLAYER_1; 
        sprite2 = PLAYER_2; 
        currentHealth = MAX_HEALTH; 
    }


    public static void updateDimensions() 
    {
        ResolutionConfig.Resolution playerSize = ResolutionConfig.getPlayerShipSize(); // Получение размеров корабля
        WIDTH = playerSize.width; 
        HEIGHT = playerSize.height; 

        ResolutionConfig.Resolution laserSize = ResolutionConfig.getLaserSize(); // Получение размеров лазера
        LASER_WIDTH = laserSize.width;
        LASER_HEIGHT = laserSize.height; 

        PLAYER_1 = ResourceLoader.loadImage("res/images/entities/player/Player1.png")
            .getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
        PLAYER_2 = ResourceLoader.loadImage("res/images/entities/player/Player2.png")
            .getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
        
        PLAYER_LASER = ResourceLoader.loadImage("res/images/entities/player/PlayerLaser.png")
            .getScaledInstance(LASER_WIDTH, LASER_HEIGHT, Image.SCALE_SMOOTH);
    }


    @Override
    public void tick() // обновления состояния корабля на каждом кадре.
    { 
        if (((GameScene) Game.getInstance().getOpenScene()).isPaused()) { // Если игра приостановлена, ничего не делать
            return;
        }
        // Считываем input'ы
        InputManager input = Game.getInstance().getInputManager(); 
        double horizontal = input.getHorizontalAxis(); 
        double vertical = input.getVerticalAxis(); 

        // Создание вектора движения на основе ввода
        Vector2 move = new Vector2(horizontal, -vertical).multiply(Game.getInstance().getDeltaTime() * SPEED);

        Vector2 position = getPosition(); // Получение текущей позиции корабля
        position.add(move); // Обновление позиции с учетом движения.

        // Определение границ, в которых может двигаться корабль.
        double leftBoundary = Main.WIDTH * 0.04;
        double rightBoundary = Main.WIDTH * 1.038;
        double topBoundary = Main.HEIGHT * 0.043;
        double bottomBoundary = Main.HEIGHT * 1.039;

        // Ограничение позиции корабля в заданных границах
        position.setX(clamp(position.getX(), leftBoundary, rightBoundary - WIDTH));
        position.setY(clamp(position.getY(), topBoundary, bottomBoundary - HEIGHT));
        
        setPosition(position); // Установка обновленной позиции


        if (input.getKey(KeyEvent.VK_SPACE)) {
            double time = Game.getInstance().getTime(); 
            if (time > nextFireTime) {
                nextFireTime = time + LASER_COOLDOWN; 
                fireLaser(); 
            }
        }


        // Чит-коды от Ромы :)
        if (input.getKeyDown(KeyEvent.VK_1)) 
        {
            currentHealth = 15;
            ResourceLoader.loadAudioClip("res/audio/RestoreHP.wav").start();
        }
        if (input.getKeyDown(KeyEvent.VK_2)) 
        {
            superLaserOn = !superLaserOn;
            ResourceLoader.loadAudioClip("res/audio/Pause.wav").start();
        }

        if (!superLaserOn) 
        {
            LASER_COOLDOWN = 0.22;
            LASER_DAMAGE_AMOUNT = 1;
            LASER_SPEED = 1500;
        }
        else 
        {
            LASER_COOLDOWN = 0.12;
            LASER_DAMAGE_AMOUNT = 1.7f;
            LASER_SPEED = 2000;
        }
    }

    // Метод для ограничения значения в заданных границах
    private double clamp(double n, double min, double max) {
        if (n < min) {
            return min; 
        } else if (n > max) {
            return max; 
        } else {
            return n; 
        }
    }


    private void fireLaser() {
        Vector2 position = getPosition();
        Laser laser = new Laser(position.add(WIDTH / 2.0 + 20, 0.0)); // Создание лазера с учетом позиции корабля.
        Game.getInstance().getOpenScene().addObject(laser); // Добавление лазера на сцену
        ResourceLoader.loadAudioClip("res/audio/PlayerLaser.wav").start();
    }


    @Override
    public void render(Graphics g) { // отрисовка корабля на экране.
        Vector2 position = getPosition(); 
        Image sprite; 
        double time = Game.getInstance().getTime(); 

        // Анимация
        if (time % 0.2 < 0.1) {
            sprite = sprite1; 
        } else {
            sprite = sprite2; 
        }

        g.drawImage(sprite, (int) (position.getX() - WIDTH / 2.0), (int) (position.getY() - HEIGHT / 2.0), null);
    }


    public double getCurrentHealth() {
        return currentHealth;
    }


    public double getMaxHealth() {
        return MAX_HEALTH;
    }


    @Override
    public void damage(double amount) {
        currentHealth -= amount; // Уменьшение текущего здоровья.
        GameScene scene = (GameScene) Game.getInstance().getOpenScene();
        scene.addObject(new DamageNotificationFilter()); // Добавление уведомления о полученном уроне.
        if (currentHealth <= 0) { 
            destroy();
            scene.endGame(); // Завершение игры.
        }
    }


    public void heal(double healAmount) {
        currentHealth += healAmount; // Увеличение текущего здоровья.
        if (currentHealth > MAX_HEALTH) { 
            currentHealth = MAX_HEALTH; 
        }
    }


    private void destroy() {
        ResourceLoader.loadAudioClip("res/audio/Explosion.wav").start();
        Game.getInstance().getOpenScene().removeObject(this); 
    }


    @Override
    public void onCollisionEnter(Entity other) {
    }


    @Override
    public void onCollisionExit(Entity other) {
    }


    public static class Laser extends Projectile 
    {

        Laser(Vector2 position) {
            super(position, new Vector2(LASER_WIDTH, LASER_HEIGHT), new Vector2(LASER_SPEED, 0)); 
        }


        @Override
        public void render(Graphics g)  // отрисовка лазера на экране.
        {
            Vector2 position = getPosition(); 
            g.drawImage(PLAYER_LASER, (int) (position.getX() - LASER_WIDTH / 2.0),
                (int) (position.getY() - LASER_HEIGHT / 2.0), null); 
        }

        // Обработка столкновения лазера с другим объектом.
        @Override
        public void onCollisionEnter(Entity other) {
            if (!(other instanceof PlayerShip) && other instanceof DamagableEntity) { // Если столкновение с врагом.
                ((DamagableEntity) other).damage(LASER_DAMAGE_AMOUNT); // Нанесение урона врагу.
                Scene scene = Game.getInstance().getOpenScene(); 
                scene.removeObject(this); // Удаление лазера из сцены.
                scene.addObject(new LaserSpark(getPosition().add(LASER_WIDTH / 2.0, 0))); // Создание эффекта искры
            } else if (other instanceof Projectile) { // Если столкновение с другим лазером.
                Scene scene = Game.getInstance().getOpenScene();
                scene.removeObject(this); // Удаление лазера из сцены.
                scene.addObject(new LaserSpark(getPosition().add(LASER_WIDTH / 2.0, 0))); // Создание эффекта искры.
            }
        }


        @Override
        public void onCollisionExit(Entity other) {
        }
    }


    private static class LaserSpark extends SceneObject 
    {
        private static final double DURATION = 0.1; // Длительность искры.
        private static final int LASER_SPARK_WIDTH = 35; 
        private static final int LASER_SPARK_HEIGHT = 35; 
        private static final Image LASER_SPARK = ResourceLoader
            .loadImage("res/images/entities/player/PlayerLaserSpark.png")
            .getScaledInstance(LASER_SPARK_WIDTH, LASER_SPARK_HEIGHT, Image.SCALE_SMOOTH); 

        private Vector2 position; // Позиция искры
        private double deathTime; // Время исчезновения искры


        private LaserSpark(Vector2 position)
         {
            this.position = position; 
            deathTime = Game.getInstance().getTime() + DURATION; 
        }

        @Override
        public void initialize() {
        }


        @Override
        public void tick() // обновления состояния искры на каждом кадре.
        {
            if (Game.getInstance().getTime() > deathTime) { 
                Game.getInstance().getOpenScene().removeObject(this); 
            }
        }


        @Override
        public void render(Graphics g) // отрисовки искры
        {
            int x = (int) (position.getX() - LASER_SPARK_WIDTH / 2.0); 
            int y = (int) (position.getY() - LASER_SPARK_HEIGHT / 2.0); 
            g.drawImage(LASER_SPARK, x, y, null);
        }

        @Override
        public void dispose() {
        }
    }


    private static class DamageNotificationFilter extends SceneObject 
    {
        private static final double DURATION = 0.25; // Длительность уведомления
        private static final double MAX_OPAQUENESS = 0.25; // Максимальная непрозрачность
        private double startTime; // Время начала уведомления


        private DamageNotificationFilter() {
            startTime = Game.getInstance().getTime(); // Установка времени начала
        }

        @Override
        public void initialize() {
        }


        @Override
        public void tick() {
        }


        @Override
        public void render(Graphics g) // отрисовка уведомления на экране.
        { 
            double time = Game.getInstance().getTime(); // Получение текущего времени.
            double elapsedTime = time - startTime; // Вычисление прошедшего времени.
            double opaqueness = 2.0 * elapsedTime / DURATION; // Вычисление непрозрачности
            if (opaqueness > 1.0) { // Если непрозрачность больше 1, инвертируем ее
                opaqueness = 2.0 - opaqueness;
            }
            if (opaqueness < 0) { // Если непрозрачность меньше 0, удаляем уведомление
                Game.getInstance().getOpenScene().removeObject(this);
                return;
            }
            int alpha = (int) (opaqueness * MAX_OPAQUENESS * 255); // Вычисление значения альфа-канала
            Color filter = new Color(255, 0, 0, alpha); // Создание красного фильтра с заданной непрозрачностью
            g.setColor(filter);
            g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT); 
        }

        @Override
        public void dispose() {
        }
    }
}
