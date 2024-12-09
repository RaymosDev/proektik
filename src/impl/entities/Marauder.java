package impl.entities;

import gameEngine.Entity;
import gameEngine.Game;
import gameEngine.ResourceLoader; 
import gameEngine.Scene;
import gameEngine.SceneObject;
import gameEngine.Vector2;
import impl.Main;
import impl.ResolutionConfig;
import impl.scenes.GameScene; 
import java.awt.Graphics;
import java.awt.Image; 

/**
 * Marauder стреляет шарами, которые разбиваются на маленькие шарики

 */
public class Marauder extends Entity implements DamagableEntity 
{
    private static int WIDTH; 
    private static int HEIGHT;
    private static final int MAX_HEALTH = 8; 
    private static final double SPEED = 200.0;
    private static final int SCORE_VALUE = 300;
    private static final double HEALTH_DROP_CHANCE = 0.25; 
    private static final double BASE_CONTACT_DAMAGE = 2.0; // Базовый урон при столкновении.
    private static int LARGE_ORB_WIDTH; 
    private static int LARGE_ORB_HEIGHT; 
    private static final double LARGE_ORB_SPEED = 500; 
    private static final double LARGE_ORB_LIFETIME = 0.7; // Время жизни большого орба
    private static final double LARGE_ORB_COOLDOWN = 3.0; // Задержка между выстрелами большого орба
    private static final double BASE_LARGE_ORB_DAMAGE = 3.0; // Базовый урон большого орба
    private static int SMALL_ORB_WIDTH; 
    private static int SMALL_ORB_HEIGHT; 
    private static final double SMALL_ORB_SPEED = 600; // Скорость маленького орба
    private static final double BASE_SMALL_ORB_DAMAGE = 1.0; // Базовый урон маленького орба

    private static Image SPRITE_1; 
    private static Image SPRITE_2; 
    private static Image LARGE_ORB; // Изображение большого орба.
    private static Image SMALL_ORB; // Изображение маленького орба.

    private double health; // Текущее здоровье
    private double nextFireTime; // Время следующего выстрела


    static {
        updateDimensions();
    }


    public Marauder(Vector2 position) 
    {
        super(position, new Vector2(WIDTH, HEIGHT)); // Вызов конструктора Entity
        health = MAX_HEALTH; 
        nextFireTime = Game.getInstance().getTime() + 2; // Установка времени следующего выстрела
    }


    public static void updateDimensions() 
    {
        ResolutionConfig.Resolution marauderSize = ResolutionConfig.getMarauderSize(); 
        WIDTH = marauderSize.width; 
        HEIGHT = marauderSize.height;

        ResolutionConfig.Resolution largeOrbSize = ResolutionConfig.getLargeOrbSize(); // Получение размеров большого орба
        LARGE_ORB_WIDTH = largeOrbSize.width; 
        LARGE_ORB_HEIGHT = largeOrbSize.height; 

        ResolutionConfig.Resolution smallOrbSize = ResolutionConfig.getSmallOrbSize(); // Получение размеров маленького орба
        SMALL_ORB_WIDTH = smallOrbSize.width; 
        SMALL_ORB_HEIGHT = smallOrbSize.height; 


        SPRITE_1 = ResourceLoader.loadImage("res/images/entities/marauder/Marauder1.png")
                .getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
        SPRITE_2 = ResourceLoader.loadImage("res/images/entities/marauder/Marauder2.png")
                .getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
        LARGE_ORB = ResourceLoader.loadImage("res/images/entities/marauder/MarauderOrb.png")
                .getScaledInstance(LARGE_ORB_WIDTH, LARGE_ORB_HEIGHT, Image.SCALE_SMOOTH);
        SMALL_ORB = ResourceLoader.loadImage("res/images/entities/marauder/MarauderOrb.png")
                .getScaledInstance(SMALL_ORB_WIDTH, SMALL_ORB_HEIGHT, Image.SCALE_SMOOTH);
    }


    @Override
    public void tick() 
    {
        Vector2 position = getPosition(); 
        position.add(-SPEED * Game.getInstance().getDeltaTime(), 0); // Движение влево.
        setPosition(position); // Установка обновленной позиции.
        double currentTime = Game.getInstance().getTime(); 
        // Проверка, пора ли стрелять орбом.
        if (currentTime > nextFireTime) {
            fireOrb();
            nextFireTime = currentTime + LARGE_ORB_COOLDOWN; 
        }
    }


    private void fireOrb() {
        Game.getInstance().getOpenScene().addObject(new LargeOrb(getPosition())); // Создание и добавление большого орба на сцену
    }


    @Override
    public void render(Graphics g) 
    {
        Vector2 position = getPosition();
        Image sprite; 
        double time = Game.getInstance().getTime(); 

        // Анимация
        if (time % 0.3 < 0.15) {
            sprite = SPRITE_1; 
        } else {
            sprite = SPRITE_2; 
        }

        g.drawImage(sprite, (int) (position.getX() - WIDTH / 2.0), (int) (position.getY() - HEIGHT / 2.0), null);
    }


    @Override
    public void damage(double amount) { // получения урона.
        health -= amount; 
        if (health <= 0) {
            destroy(); 
        }
    }


    private void destroy() {
        GameScene scene = (GameScene) Game.getInstance().getOpenScene(); 
        scene.removeObject(this); 
        scene.addScore(SCORE_VALUE);
        Explosion explosion = new Explosion(getPosition(), 300, 0.4); // Создание взрыва
        scene.addObject(explosion); // Добавление взрыва на сцену.
        // Вероятность выпадения HealthDrop.
        if (Math.random() < HEALTH_DROP_CHANCE) {
            scene.addObject(new HealthDrop(getPosition())); // Добавление HealthDrop на сцену.
        }
    }


    @Override
    public void onCollisionEnter(Entity other) {   // Обработка столкновения с другим объектом.
        if (other instanceof PlayerShip) { // Если столкновение с кораблем игрока.
            ((PlayerShip) other).damage(Main.difficulty.getModifier() * BASE_CONTACT_DAMAGE); // Нанесение урона игроку.
            destroy();
        }
    }


    @Override
    public void onCollisionExit(Entity other) {    
    }


    private static class LargeOrb extends Projectile {
        private double deathTime; // Время исчезновения орба.


        public LargeOrb(Vector2 position) {
            super(position, new Vector2(LARGE_ORB_WIDTH, LARGE_ORB_HEIGHT), new Vector2(-LARGE_ORB_SPEED, 0)); 
            deathTime = Game.getInstance().getTime() + LARGE_ORB_LIFETIME; 
        }

        @Override
        public void initialize() {
            super.initialize(); // Вызов инициализации родительского класса.
            ResourceLoader.loadAudioClip("res/audio/MarauderLargeOrbFire.wav").start(); 
        }


        @Override
        public void tick() { // обновления состояния орба на каждом кадре.
            super.tick(); 
            if (Game.getInstance().getTime() > deathTime) { 
                blowUp(); 
            }
        }

        // Метод для взрыва орба
        private void blowUp() 
        {
            ResourceLoader.loadAudioClip("res/audio/MarauderLargeOrbBlowUp.wav").start(); 
            Scene scene = Game.getInstance().getOpenScene(); // Получение текущей сцены.
            scene.removeObject(this); // Удаление орба из сцены
            Vector2 smallOrbVelocity = Vector2.fromAngle(2.0 * Math.PI / 3.0, SMALL_ORB_SPEED); // Вычисление начальной скорости маленьких орбов
            for (int i = 0; i < 5; i++) { // Создание 5 маленьких орбов
                scene.addObject(new SmallOrb(getPosition(), smallOrbVelocity)); // Добавление маленького орба на сцену
                smallOrbVelocity.rotate(Math.PI / 6.0); // Поворот скорости для следующего орба
            }
        }

        // Обработка столкновения большого орба с другим объектом.
        @Override
        public void onCollisionEnter(Entity other) 
        {
            if (other instanceof PlayerShip) { // Если столкновение с кораблем игрока.
                ((PlayerShip) other).damage(BASE_LARGE_ORB_DAMAGE); // Нанесение урона игроку.
                Scene scene = Game.getInstance().getOpenScene();
                scene.removeObject(this); 
                scene.addObject(new OrbSpark(getPosition())); // Создание эффекта искры
            } else if (other instanceof PlayerShip.Laser) { // Если столкновение с лазером игрока
                Scene scene = Game.getInstance().getOpenScene(); // Получение текущей сцены
                scene.removeObject(this); 
                scene.addObject(new OrbSpark(getPosition())); // Создание эффекта искры
                blowUp(); 
            }
        }


        @Override
        public void onCollisionExit(Entity other) {
        }


        @Override
        public void render(Graphics g) { // отрисовка большого орба
            Vector2 position = getPosition(); // Получение текущей позиции орба.
            int x = (int) (position.getX() - LARGE_ORB_WIDTH / 2.0);
            int y = (int) (position.getY() - LARGE_ORB_HEIGHT / 2.0); 
            g.drawImage(LARGE_ORB, x, y, null); // Отрисовка большого орба
        }
    }


    private static class SmallOrb extends Projectile 
    {

        public SmallOrb(Vector2 position, Vector2 velocity) {
            super(position, new Vector2(SMALL_ORB_WIDTH, SMALL_ORB_HEIGHT), velocity); 
        }

        @Override
        public void initialize() {
            super.initialize();
        }


        @Override
        public void onCollisionEnter(Entity other) { // Обработка столкновения маленького орба с другим объектом.
            if (other instanceof PlayerShip) { // Если столкновение с кораблем игрока.
                ((PlayerShip) other).damage(BASE_SMALL_ORB_DAMAGE * Main.difficulty.getModifier()); // Нанесение урона игроку
                Scene scene = Game.getInstance().getOpenScene(); 
                scene.removeObject(this);
                scene.addObject(new OrbSpark(getPosition().add(SMALL_ORB_WIDTH / 2.0, 0))); // Создание эффекта искры
            } else if (other instanceof PlayerShip.Laser) { // Если столкновение с лазером игрока.
                Scene scene = Game.getInstance().getOpenScene(); 
                scene.removeObject(this); 
                scene.addObject(new OrbSpark(getPosition().add(SMALL_ORB_WIDTH / 2.0, 0))); // Создание эффекта искры
            }
        }


        @Override
        public void onCollisionExit(Entity other) {
        }


        @Override
        public void render(Graphics g) { // отрисовка маленького орба на экране.
            Vector2 position = getPosition(); // Получение текущей позиции маленького орба.
            g.drawImage(SMALL_ORB, (int) (position.getX() - SMALL_ORB_WIDTH / 2.0),
                    (int) (position.getY() - SMALL_ORB_HEIGHT / 2.0), null); // Отрисовка маленького орба
        }

        @Override
        public void dispose() {
            super.dispose(); 
        }
    }


    private static class OrbSpark extends SceneObject 
    {
        private static final double DURATION = 0.1; // Длительность искры.
        private static final int ORB_SPARK_WIDTH = 50;
        private static final int ORB_SPARK_HEIGHT = 50; 
        private static final Image ORB_SPARK = ResourceLoader
                .loadImage("res/images/entities/marauder/MarauderOrbSpark.png")
                .getScaledInstance(ORB_SPARK_WIDTH, ORB_SPARK_HEIGHT, 0); 

        private Vector2 position; // Позиция искры
        private double deathTime; // Время исчезновения искры


        private OrbSpark(Vector2 position) 
        {
            this.position = position; 
            deathTime = Game.getInstance().getTime() + DURATION; 
        }

        @Override
        public void initialize() {
        }


        @Override
        public void tick() { // обновление состояния искры на каждом кадре.
            if (Game.getInstance().getTime() > deathTime) {
                Game.getInstance().getOpenScene().removeObject(this); 
            }
        }

        // отрисовки искры 
        @Override
        public void render(Graphics g) {
            int x = (int) (position.getX() - ORB_SPARK_WIDTH / 2.0);
            int y = (int) (position.getY() - ORB_SPARK_HEIGHT / 2.0); 
            g.drawImage(ORB_SPARK, x, y, null); 
        }

        @Override
        public void dispose() {
        }
    }
}
