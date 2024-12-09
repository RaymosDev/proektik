package impl.scenes; 

/*  Scene где считываем ввод для управления меню
*/

import gameEngine.InputManager;
import gameEngine.ResourceLoader; 
import gameEngine.Scene;
import impl.Main; 
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent; 

public class SceneWithKeys extends Scene {
    private static final Font UI_FONT = ResourceLoader.loadFont("res/Font.ttf", 36); 

    // Изображения кнопок: выделенная и обычная
    public final Image buttonSelected = ResourceLoader.loadImage("res/images/ui/ButtonHover.png").getScaledInstance(336, 56, 0);
    public final Image button = ResourceLoader.loadImage("res/images/ui/Button.png").getScaledInstance(336, 56, 0);


    public void initialize() {
    }

    // Отрисовка прокручиваемого меню.
    public void renderScrollingMenus(Graphics g, String[] options, int currentOption) {
        Image buttonState; // Тут храненим состояния кнопки (выделенная/обычная)
        g.setFont(UI_FONT); 
        int containerWidth = Main.WIDTH; // ширина контейнера
        int offset = 120; // Смещение для вертикального размещения меню
        int startY = (Main.HEIGHT - (options.length * 67)) / 2 + offset; // Вычисление начальной позиции по Y для меню

        // Цикл по всем опциям меню
        for (int i = 0; i < options.length; i++) 
        {
            if (currentOption == i) {
                g.setColor(Color.WHITE); 
                buttonState = buttonSelected; 
            } else {
                g.setColor(Color.WHITE); 
                buttonState = button;
            }

            // Вычисление координат для отрисовки кнопки.
            int buttonX = (containerWidth - buttonState.getWidth(null)) / 2; // Центрирование кнопки по X
            g.drawImage(buttonState, buttonX, startY + 67 * i, null); // Отрисовка кнопки

            String option = options[i]; // Получение текста текущей опции
            int width = g.getFontMetrics().stringWidth(option); // Получение ширины текста опции
            int textX = (containerWidth - width) / 2; // Центрирование текста по X.
            g.drawString(option, textX, startY + 39 + 67 * i); // Отрисовка текста опции
        }
    }

    // Обработка навигации вверх и вниз по опциям меню.
    public int upDown(InputManager inputManager, String[] options, int currentOption) 
    {
        if (inputManager.getKeyDown(KeyEvent.VK_DOWN)) {
            onSound(); 
            currentOption++; // Переход к следующей опции.
            if (currentOption >= options.length) { // Если текущая опция выходит за пределы массива, возврат к первой опции.
                return 0; 
            }
        }
        if (inputManager.getKeyDown(KeyEvent.VK_UP)) {
            onSound(); 
            currentOption--; 
            if (currentOption < 0) { // Если текущая опция меньше нуля, возврат к последней опции.
                return options.length - 1;
            }
        }
        return currentOption; // Возврат текущей опции.
    }

    public void onSound() {
        ResourceLoader.loadAudioClip("res/audio/Button.wav").start(); // Воспроизведение звука кнопки
    }
}
