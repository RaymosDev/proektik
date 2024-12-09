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

public class AsteroidSmall extends Entity implements DamagableEntity 
{ 
    private static final double BASE_DAMAGE_AMOUNT = 1; // Базовое количество урона, наносимого маленьким астероидом.
    private static final double MAX_HEALTH = 3; 
    private static final double BASE_SPEED = 500; 
    private static final int BASE_SCORE_VALUE = 20; 

    private static int WIDTH; // Ширина маленького астероида
    private static int HEIGHT; // Высота маленького астероида
    private static Image SPRITE; // Изображение спрайта маленького астероида

    private Vector2 velocity; // Вектор скорости маленького астероида
    private double currentHealth; // Текущее здоровье маленького астероида

    static { 
        updateDimensions(); 
    }


    public AsteroidSmall(Vector2 position, Vector2 direction) {
        super(position, new Vector2(WIDTH, HEIGHT)); // Вызов конструктора Entity
        double difficultyModifier = Main.difficulty.getModifier(); // Получение модификатора сложности
        this.velocity = direction.clone().normalize().multiply(BASE_SPEED * difficultyModifier); // Инициализация скорости маленького астероида
        currentHealth = MAX_HEALTH * difficultyModifier; // Установка текущего здоровья с учетом модификатора сложности
    }


    public static void updateDimensions() 
    {
        ResolutionConfig.Resolution currentResolution = ResolutionConfig.getAsteroidSmallSize(); 
        WIDTH = currentResolution.width; 
        HEIGHT = currentResolution.height;

        SPRITE = ResourceLoader.loadImage("res/images/entities/asteroids/AsteroidSmall.png")
                .getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
    }

    @Override
    public void tick()  // обновление состояния маленького астероида
    {
        Vector2 position = getPosition(); 
        position.add(velocity.clone().multiply(Game.getInstance().getDeltaTime())); 
        setPosition(position); // Установка новой позиции
    }

    @Override
    public void render(Graphics g) // отрисовка маленького астероида
    { 
        Vector2 position = getPosition(); 
        g.drawImage(SPRITE, (int) position.getX() - WIDTH / 2, (int) position.getY() - HEIGHT / 2, null);
    }

    @Override
    public void onCollisionEnter(Entity other) // Обработка столкновения с другим объектом
    { 
        if (other instanceof PlayerShip) // Если столкновение с кораблем игрока
        { 
            ((PlayerShip) other).damage(BASE_DAMAGE_AMOUNT * Main.difficulty.getModifier()); // Наносим урон кораблю игрока
            destroy(); // Уничтожаем маленький астероид
        }
    }

    @Override
    public void onCollisionExit(Entity other) { 
    }

    @Override
    public void damage(double amount) { // Получения урона
        currentHealth -= amount;
        if (currentHealth <= 0) {
            destroy(); 
        }
    }

    private void destroy() { 
        GameScene scene = (GameScene) Game.getInstance().getOpenScene();
        scene.removeObject(this); 
        scene.addScore((int) (BASE_SCORE_VALUE * Main.difficulty.getModifier())); 
        Explosion explosion = new Explosion(getPosition(), 100, 0.2);
        scene.addObject(explosion); 
    }
}
