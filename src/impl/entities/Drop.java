package impl.entities; 

import gameEngine.Entity;
import gameEngine.Game; 
import gameEngine.Vector2; 

/**
 * Drop это объект, который подобрать игрок.
 */

public abstract class Drop extends Entity 
{
    private double deathTime; // Игровое время, когда объект должен исчезнуть
    private double startY; // Начальная позиция по оси Y (используется для анимации)


    public Drop(Vector2 position, Vector2 size, double lifetime) 
    {
        super(position, size); // Вызов конструктора Entity
        deathTime = Game.getInstance().getTime() + lifetime; // Вычисление времени смерти объекта
        startY = position.getY(); // Запоминание начальной позиции по оси Y
    }

    @Override
    public void tick() // Обновление состояния объекта
    { 
        double time = Game.getInstance().getTime(); // Получение текущего времени
        if (time >= deathTime) 
        { 
            Game.getInstance().getOpenScene().removeObject(this); 
        } 
        else 
        {
            Vector2 position = getPosition(); // Получение текущей позиции объекта
            position.setX(position.getX() - 150.0 * Game.getInstance().getDeltaTime());
            position.setY(startY + 15.0 * Math.sin(2.0 * (deathTime - time))); 
            setPosition(position); // Установка новой позиции объекта
        }
    }

    @Override
    public void onCollisionEnter(Entity other) // Обработка столкновения с другим объектом
    { 
        if (other instanceof PlayerShip) // Если столкновение с кораблем игрока
        { 
            onPickup((PlayerShip) other); 
            Game.getInstance().getOpenScene().removeObject(this); 
        }
    }

    @Override
    public void onCollisionExit(Entity other) { // Jбработка выхода из столкновения
    }

    // Метод подбора дропа (по сути реализован только у HealthDrop)
    public abstract void onPickup(PlayerShip player);
}