package impl.entities;

import gameEngine.Entity;
import gameEngine.Game;
import gameEngine.ResourceLoader;
import gameEngine.Vector2;
import impl.Main;
import impl.ResolutionConfig;
import impl.scenes.GameScene;
import java.awt.Graphics; 
import java.awt.Image;

public class AsteroidLarge extends Entity implements DamagableEntity 
{ 
    private static final double BASE_DAMAGE_AMOUNT = 3; // Базовое количество урона, наносимого астероидом
    private static final double MAX_HEALTH = 5; 
    private static final double BASE_SPEED = 300; 
    private static final int BASE_SCORE_VALUE = 50; 

    private static int WIDTH; 
    private static int HEIGHT; 
    private static Image SPRITE_1; 
    private static Image SPRITE_2; 
    private static Image SPRITE_3;

    private Vector2 velocity; // Вектор скорости астероида
    private double currentHealth; // Текущее здоровье астероида


    static { 
        updateDimensions(); 
    }


    public AsteroidLarge(Vector2 position, Vector2 direction) 
    {
        super(position, new Vector2(WIDTH, HEIGHT)); // Вызов конструктора Entity
        double difficultyModifier = Main.difficulty.getModifier(); // Получение модификатора сложности
        this.velocity = direction.clone().normalize().multiply(BASE_SPEED * difficultyModifier); // Инициализация скорости астероида
        currentHealth = MAX_HEALTH; // Установка текущего здоровья на максимальное значение
    }

    public static void updateDimensions() 
    {
        ResolutionConfig.Resolution currentResolution = ResolutionConfig.getAsteroidSize(); 
        WIDTH = currentResolution.width;
        HEIGHT = currentResolution.height; 

        // Загрузка и масштабирование спрайтов астероида.
        SPRITE_1 = ResourceLoader.loadImage("res/images/entities/asteroids/AsteroidLarge1.png")
                .getScaledInstance(WIDTH, HEIGHT, 0);
        SPRITE_2 = ResourceLoader.loadImage("res/images/entities/asteroids/AsteroidLarge2.png")
                .getScaledInstance(WIDTH, HEIGHT, 0);
        SPRITE_3 = ResourceLoader.loadImage("res/images/entities/asteroids/AsteroidLarge3.png")
                .getScaledInstance(WIDTH, HEIGHT, 0);
    }

    @Override
    public void tick() // Обновления состояния астероида
    { 
        Vector2 position = getPosition(); 
        position.add(velocity.clone().multiply(Game.getInstance().getDeltaTime())); // Обновление позиции с учетом скорости и времени
        setPosition(position); // Установка новой позиции
    }

    @Override
    public void render(Graphics g) // Отрисовка астероида
    { 
        Vector2 position = getPosition(); 
        Image sprite; 

        // Определение спрайта в зависимости от текущего здоровья
        if (currentHealth < 3) 
        {
            sprite = SPRITE_3; 
        } 
        else if (currentHealth < 5) 
        {
            sprite = SPRITE_2; 
        } 
        else 
        {
            sprite = SPRITE_1;
        }

        g.drawImage(sprite, (int) position.getX() - WIDTH / 2, (int) position.getY() - HEIGHT / 2, null);
    }

    @Override
    public void onCollisionEnter(Entity other) // Обработка столкновения с другим объектом
    { 
        if (other instanceof PlayerShip) // Если столкновение с кораблем игрока
        { 
            ((PlayerShip) other).damage(BASE_DAMAGE_AMOUNT * Main.difficulty.getModifier()); // Наносим урон кораблю игрока
            destroyWithExplosion(false); // Уничтожаем астероид с эффектом взрыва
        }
    }

    @Override
    public void onCollisionExit(Entity other) { 
    }

    @Override
    public void damage(double amount) // Получение урона
    {
        currentHealth -= amount; 
        if (currentHealth <= 0) 
        {
            destroy(true); 
        }
    }

    private void destroy(boolean split) // Уничтожения астероида С ВОЗМОЖНОСТЬЮ ДЕЛЕНИЯ
    { 
        ResourceLoader.loadAudioClip("res/audio/AsteroidHit.wav").start(); 
        GameScene scene = (GameScene) Game.getInstance().getOpenScene(); // Получение текущей открытой игровой сцены.
        scene.addScore((int) (BASE_SCORE_VALUE * Main.difficulty.getModifier())); // Добавление очков за уничтожение астероида.

        scene.removeObject(this); 
        if (split) 
        { 
            // На месте где был текущей большой астеройд создаем три маленьких
            Vector2 position = getPosition(); 
            scene.addObject(new AsteroidSmall(position, velocity)); 
            velocity.rotate(-Math.toRadians(30)); 
            scene.addObject(new AsteroidSmall(position, velocity)); 
            velocity.rotate(Math.toRadians(60)); 
            scene.addObject(new AsteroidSmall(position, velocity)); 
        }
    }


    // Пришлось добавить точно такой же метод как сверху, но в верхнем нет взрыва, в этом есть.
    private void destroyWithExplosion(boolean split) 
    { 
        ResourceLoader.loadAudioClip("res/audio/AsteroidHit.wav").start(); 
        GameScene scene = (GameScene) Game.getInstance().getOpenScene(); 
        scene.addScore((int) (BASE_SCORE_VALUE * Main.difficulty.getModifier())); 
        Explosion explosion = new Explosion(getPosition(), 200, 0.4);
        scene.addObject(explosion); 
        scene.removeObject(this); 
        if (split) 
        {
            Vector2 position = getPosition();
            scene.addObject(new AsteroidSmall(position, velocity)); 
            velocity.rotate(-Math.toRadians(30)); 
            scene.addObject(new AsteroidSmall(position, velocity)); 
            velocity.rotate(Math.toRadians(60));
            scene.addObject(new AsteroidSmall(position, velocity)); 
        }
    }
} 
