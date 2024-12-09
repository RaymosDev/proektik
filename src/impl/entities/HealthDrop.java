package impl.entities;

import gameEngine.Game; 
import gameEngine.ResourceLoader;
import gameEngine.Vector2;
import impl.PlayerFollowingText;
import impl.ResolutionConfig;
import java.awt.Graphics;
import java.awt.Image; 

/**
 *  Просто хилка, наследуется от Drop
 */

public class HealthDrop extends Drop 
{
    private static final double HEAL_AMOUNT = 4; // Сколько восстанавливаем здоровья
    private static final double LIFETIME = 10.0; // Время жизни объекта HealthDrop

    private static int WIDTH; 
    private static int HEIGHT; 
    private static Image SPRITE; 


    static {
        updateDimensions();
    }


    public HealthDrop(Vector2 position) 
    {
        super(position, new Vector2(WIDTH, HEIGHT), LIFETIME); // Вызов конструктора Drop
    }


    public static void updateDimensions() // Обновление размеров объекта в зависимости от текущего разрешения
    {
        ResolutionConfig.Resolution currentResolution = ResolutionConfig.getHealthDropSize(); // Получение текущего разрешения для HealthDrop.
        WIDTH = currentResolution.width; // Установка ширины объекта.
        HEIGHT = currentResolution.height; // Установка высоты объекта.
        // Загрузка и масштабирование изображения для HealthDrop.
        SPRITE = ResourceLoader.loadImage("res/images/entities/drops/HealthDrop.png")
                .getScaledInstance(WIDTH, HEIGHT, 0);
    }


    @Override
    public void onPickup(PlayerShip player)  // Подбор объекта игроком
    {
        player.heal(HEAL_AMOUNT); // +hp игроку
        ResourceLoader.loadAudioClip("res/audio/RestoreHP.wav").start(); 
        PlayerFollowingText text = new PlayerFollowingText("+" + (int) HEAL_AMOUNT + " HP"); // пишем текстом +hp
        Game.getInstance().getOpenScene().addObject(text);
    }


    @Override
    public void render(Graphics g) // отрисовка объекта на экране
    {
        Vector2 position = getPosition(); 
        g.drawImage(SPRITE, (int) (position.getX() - WIDTH / 2.0), (int) (position.getY() - HEIGHT / 2.0), null); // отрисовка с центрированием по координатам
    }
}
