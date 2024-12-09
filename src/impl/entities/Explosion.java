package impl.entities; 

import gameEngine.Entity;
import gameEngine.Game; 
import gameEngine.ResourceLoader;
import gameEngine.Vector2; 
import java.awt.Graphics;
import java.awt.Image; 

/**
 */

public class Explosion extends Entity 
{
    private static final Image[] FRAMES = {
        ResourceLoader.loadImage("res/images/entities/explosion/Explosion1.png"),
        ResourceLoader.loadImage("res/images/entities/explosion/Explosion2.png"),
        ResourceLoader.loadImage("res/images/entities/explosion/Explosion3.png"),
        ResourceLoader.loadImage("res/images/entities/explosion/Explosion4.png"),
        ResourceLoader.loadImage("res/images/entities/explosion/Explosion5.png"),
        ResourceLoader.loadImage("res/images/entities/explosion/Explosion6.png"),
        ResourceLoader.loadImage("res/images/entities/explosion/Explosion7.png")
    };

    private double lifetime; // Время жизни анимации взрыва
    private double startTime; // Время, когда анимация была запущена
    private Image[] scaledFrames; // Массив кадров, масштабированных до заданного размера


    public Explosion(Vector2 position, int size, double lifetime) 
    {
        super(position, new Vector2(size, size)); // Вызов конструктора Entity
        this.lifetime = lifetime; 
        startTime = Game.getInstance().getTime(); 
        scaledFrames = new Image[FRAMES.length]; 
        for (int i = 0; i < FRAMES.length; i++) 
        {           
            scaledFrames[i] = FRAMES[i].getScaledInstance(size, size, Image.SCALE_FAST); // Масштабирование каждого кадра до заданного размера (в зависимости от объекта, который взорвался, взрыв будет разного размера)
        }
    }

    @Override
    public void initialize() 
    { 
        super.initialize(); // инициализируем родительский класс
        ResourceLoader.loadAudioClip("res/audio/Explosion.wav").start();
    }

    @Override
    public void tick() { 
    }

    @Override
    public void render(Graphics g) // отрисовка анимации 
    { 
        double time = Game.getInstance().getTime(); // Получение текущего игрового времени
        double proportion = (time - startTime) / lifetime; // Вычисление пропорции времени жизни
        int frameIndex = (int) (proportion * FRAMES.length); // Определение текущего кадра анимации
        if (frameIndex >= FRAMES.length) // Если анимация закончилась
        { 
            Game.getInstance().getOpenScene().removeObject(this); 
            return;
        }
        Image frame = scaledFrames[frameIndex]; // Получение текущего кадра для отображения.
        Vector2 position = getPosition(); 
        Vector2 size = getSize();
        // Вычисление координат для отрисовки кадра, чтобы центрировать его
        int x = (int) (position.getX() - size.getX() / 2.0);
        int y = (int) (position.getY() - size.getY() / 2.0);
        g.drawImage(frame, x, y, null); // рисуем кадр
    }

    @Override
    public void onCollisionEnter(Entity other) { 
    }

    @Override
    public void onCollisionExit(Entity other) {
    }
}
