package impl.scenes; 

import gameEngine.Game;
import gameEngine.SceneObject;
import impl.Main; 
import java.awt.Color;
import java.awt.Graphics; 

/**
 *  Эффект проявления (обратное затухание по сути) 
 */
public class FadeIn extends SceneObject {
    private double duration; // Длительность эффекта затухания
    private double startTime; // Время начала эффекта затухания


    public FadeIn(double duration) {
        this.duration = duration; 
    }


    @Override
    public void initialize() {
        startTime = Game.getInstance().getTime(); // Запись времени начала затухания
    }


    @Override
    public void tick() {
    }


    @Override
    public void render(Graphics g) {
        double time = Game.getInstance().getTime(); 
        
        // Проверка, завершился ли эффект затухания.
        if (time > startTime + duration) {
            Game.getInstance().getOpenScene().removeObject(this); // Удаление объекта затухания из текущей сцены
            return; 
        }
        
        // Вычисление альфа-канала для цвета
        int alpha = (int) (255 - (time - startTime) / duration * 255.0);
        Color color = new Color(0, 0, 0, alpha); // Черный цвет с вычисленным альфа-каналом.
        
        g.setColor(color); 
        g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT); // Прямоугольник на весь экран
    }


    @Override
    public void dispose() {

    }
}
