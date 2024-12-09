package impl.scenes; 

/*  Просто главное меню
*/

import gameEngine.Game;
import gameEngine.InputManager;
import gameEngine.ResourceLoader; 
import impl.Difficulty;
import impl.Main;
import impl.entities.AsteroidLarge;
import impl.entities.AsteroidSmall;
import impl.entities.HealthDrop;
import impl.entities.Hornet; 
import impl.entities.Javelin;
import impl.entities.Marauder;
import impl.entities.PlayerShip; 
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.sound.sampled.Clip; 

public class MainMenuScene extends SceneWithKeys {
    private static final Font UI_FONT = ResourceLoader.loadFont("res/Font.ttf", 36); 
    
    // Опции главного меню.
    private final String[] MAIN_MENU_OPTIONS = { "Старт", "Сложность", "Разрешение", "Титры", "Выйти" };
    private final String[] SETTINGS_OPTIONS = { "Простая", "Средняя", "Высокая", "Назад" }; 
    private final String[] RESOLUTION_OPTIONS = { "1270x610", "1800x800", "Назад" }; 

    private BufferedImage backgroundImage; 
    private Image title; 
    private Clip backgroundMusic; 

    private int currentOption; // Текущая выбранная опция в меню

    private String sceneOption; // текущее окно в меню (сложность, титры, ...)


    public MainMenuScene(String sceneOption) {
        this.sceneOption = sceneOption;
    }

    public MainMenuScene() {
        this.sceneOption = "Main";
    }


    @Override
    public void initialize() {

        backgroundImage = ResourceLoader.toBufferedImage(
            ResourceLoader.loadImage("res/images/backgrounds/GameBackground.png").getScaledInstance(24750, 825, 0));
        title = ResourceLoader.loadImage("res/images/ui/Title.png").getScaledInstance(889, 322, 0);


        backgroundMusic = ResourceLoader.loadAudioClip("res/audio/MainMenuMusic.wav");
        backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        addObject(new FadeIn(1.0)); // Добавление эффекта затухания при входе в сцену
    }

    // Отрисовки сцены
    @Override
    public void render(Graphics g) 
    {
        InputManager inputManager = Game.getInstance().getInputManager(); 
        g.setFont(UI_FONT); 
        g.setColor(Color.WHITE); 

        double time = Game.getInstance().getTime(); 
        int x = (int) (time * 50 % 22195); // Вычисление смещения для фона
        Image backgroundSubImage = backgroundImage.getSubimage(x, 0, Main.WIDTH, Main.HEIGHT); 
        g.drawImage(backgroundSubImage, 0, 0, null); // Отрисовка фона


        if (sceneOption.equals("Main")) 
        {
            int imageWidth = title.getWidth(null); 
            int xTitle = (Main.WIDTH - imageWidth) / 2; 
            g.drawImage(title, xTitle, 0, null); // Отрисовка заголовка

            currentOption = upDown(inputManager, MAIN_MENU_OPTIONS, currentOption); // Обработка выбора в главном меню.
            renderScrollingMenus(g, MAIN_MENU_OPTIONS, currentOption); // Отрисовка меню
            mainMenuEnter(inputManager); // Обработка ввода в главном меню.
        } 
        else if (sceneOption.equals("Difficulty")) 
        {
            int imageWidth = title.getWidth(null);
            int xTitle = (Main.WIDTH - imageWidth) / 2;
            g.drawImage(title, xTitle, 0, null); 

            currentOption = upDown(inputManager, SETTINGS_OPTIONS, currentOption); 
            renderScrollingMenus(g, SETTINGS_OPTIONS, currentOption); 
            settingsMenuEnter(inputManager); 
        } 
        else if (sceneOption.equals("Resolution")) 
        {
            int imageWidth = title.getWidth(null);
            int xTitle = (Main.WIDTH - imageWidth) / 2;
            g.drawImage(title, xTitle, 0, null);

            currentOption = upDown(inputManager, RESOLUTION_OPTIONS, currentOption); 
            renderScrollingMenus(g, RESOLUTION_OPTIONS, currentOption);
            resolutionMenuEnter(inputManager);
        } 
        else if (sceneOption.equals("Credits")) 
        {
            creditsScene(g, inputManager); // Отрисовка сцены титров
        }
        super.render(g);
    }

    // Обработка ввода в главном меню.
    public void mainMenuEnter(InputManager inputManager) 
    {
        if (inputManager.getKeyDown(KeyEvent.VK_ENTER)) { 
            onSound(); 
            if (currentOption == 0) { // Если выбран "Старт".
                backgroundMusic.stop(); 
                Game.getInstance().loadScene(new GameScene()); 
            } else if (currentOption == 1) { // Если выбрана "Сложность".
                sceneOption = "Difficulty"; 
                currentOption = Main.difficulty.ordinal(); 
            } else if (currentOption == 2) { // Если выбрано "Разрешение".
                sceneOption = "Resolution"; 
                currentOption = (Main.WIDTH == 1270) ? 0 : 1; 
            } else if (currentOption == 3) { // Если выбраны "Титры".
                sceneOption = "Credits"; 
                addObject(new FadeIn(1.0)); 
            } else if (currentOption == 4) { // Если выбрано "Выйти".
                Game.getInstance().stop(); 
            }
        }
    }

    // Обработка ввода в меню настроек
    public void settingsMenuEnter(InputManager inputManager) 
    {
        if (inputManager.getKeyDown(KeyEvent.VK_ENTER)) {
            onSound(); 
            if (currentOption == 0) { // "Простая".
                Main.difficulty = Difficulty.EASY; 
            } else if (currentOption == 1) { // "Средняя".
                Main.difficulty = Difficulty.MEDIUM; 
            } else if (currentOption == 2) { // "Высокая".
                Main.difficulty = Difficulty.HARD; 
            }
            currentOption = 1; // Сброс текущей опции на "Средняя".
            sceneOption = "Main"; // Возврат к главному меню.
        }
    }

    // Обработка ввода в меню разрешения
    public void resolutionMenuEnter(InputManager inputManager) 
    {
        if (inputManager.getKeyDown(KeyEvent.VK_ENTER)) { 
            onSound(); 
            if (currentOption == 2) { // Если выбрано "Назад".
                sceneOption = "Main"; // Возврат к главному меню
                return; 
            }
            // Установка нового разрешения в зависимости от выбранной опции
            int newWidth = (currentOption == 0) ? 1270 : 1800;
            int newHeight = (currentOption == 0) ? 640 : 800;

            // Если новое разрешение отличается от текущего --> обновление разрешения
            if (Main.WIDTH != newWidth || Main.HEIGHT != newHeight) {
                Main.WIDTH = newWidth; 
                Main.HEIGHT = newHeight; 
                Game.getInstance().getDisplay().resize(Main.WIDTH, Main.HEIGHT); 
                // Обновление размеров всех сущностей
                HealthDrop.updateDimensions();
                AsteroidLarge.updateDimensions();
                PlayerShip.updateDimensions();
                AsteroidSmall.updateDimensions();
                Hornet.updateDimensions();
                Javelin.updateDimensions();
                Marauder.updateDimensions();
            }

            currentOption = 2; // Сброс текущей опции на "Назад"
            sceneOption = "Main"; // Возврат к главному меню
        }
    }

    // Отрисовка сцены титров
    public void creditsScene(Graphics g, InputManager inputManager) 
    {
        g.setFont(UI_FONT);
        String title = "ТИТРЫ"; 
        int containerWidth = Main.WIDTH; 

        int titleWidth = g.getFontMetrics().stringWidth(title); // Получение ширины заголовка.
        int titleX = (containerWidth - titleWidth) / 2; // Вычисление позиции по центру
        g.drawString(title, titleX, 105); 


        String[] lines = { "Разработчики:",
            "", "Мищиряков Р. А.", "Степанов М. Д.", "Евдокимов П. С.", "",
        };
    
        // Отрисовка строк с именами
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int width = g.getFontMetrics().stringWidth(line); 
            int textX = (containerWidth - width) / 2; 
            g.drawString(line, textX, 190 + i * 50); 
        }
        
        returnToMenuOption(g, inputManager); // Возврат к меню
    }

    // Обработка возврата к меню
    public void returnToMenuOption(Graphics g, InputManager inputManager) 
    {
        g.setFont(UI_FONT);
        String prompt = "НАЖМИТЕ ENTER"; 
        int containerWidth = Main.WIDTH;

        // Мигающее сообщение
        if (Game.getInstance().getTime() % 1.5 < 0.9) {
            int textWidth = g.getFontMetrics().stringWidth(prompt);
            int textX = (containerWidth - textWidth) / 2;
            g.drawString(prompt, textX, 600); 
        }

        if (inputManager.getKeyDown(KeyEvent.VK_ENTER)) {
            currentOption = 0; // Сброс текущей опции
            onSound(); 
            addObject(new FadeIn(1.0));
            sceneOption = "Main"; 
        }
    }
}
