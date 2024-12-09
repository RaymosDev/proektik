package impl.entities; 

import gameEngine.Entity;
import gameEngine.Game; 
import gameEngine.Vector2; 


public abstract class Projectile extends Entity {
    private Vector2 velocity; // скорость снаряда.


    public Projectile(Vector2 position, Vector2 size, Vector2 velocity) 
    {
        super(position, size); // Вызов конструктора Entity
        this.velocity = velocity.clone(); // Клонирование вектора скорости 
    }


    @Override
    public void tick() { 
        Vector2 position = getPosition(); // Получение текущей позиции снаряда
        position.add(velocity.clone().multiply(Game.getInstance().getDeltaTime())); // Обновление позиции
        setPosition(position); // Установка обновленной позиции снаряда
    }
}
