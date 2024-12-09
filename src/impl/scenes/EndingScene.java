package impl.scenes; 

import gameEngine.Game;
import gameEngine.InputManager;
import gameEngine.ResourceLoader;
import impl.Main;
import java.awt.Color;
import java.awt.Font; 
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.sound.sampled.Clip; 

public class EndingScene extends SceneWithKeys {
    private Image backgroundImage; // Фон
    private Clip backgroundMusic; // Фоновая музыка
    private int currentOption; // Текущий выбранный вариант в меню
    private int score; // Счет игрока
    private boolean wasNewHighScore; 
    private int highScore; 
    private String os; // Операционная система


    private static final Font DEATH_FONT = ResourceLoader.loadFont("res/Font.ttf", 50);
    private static final Font SCORE_FONT = ResourceLoader.loadFont("res/Font.ttf", 50);
    private static final Font UI_FONT = ResourceLoader.loadFont("res/Font.ttf", 36);

    // Варианты меню
    private final String[] ENDING_OPTIONS = { "Перезапуск", "Главное меню", "Выйти" };


    public EndingScene(int score) 
    {
        this.score = score; 

        backgroundImage = ResourceLoader.loadImage("res/images/backgrounds/GameBackground.png")
            .getScaledInstance(24750, 825, 0);
    }


    public void initialize() 
    {
        backgroundMusic = ResourceLoader.loadAudioClip("res/audio/DeathScene.wav");
        backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        addObject(new FadeIn(5.0)); 
        os = System.getProperty("os.name");

        try {
            highScore = getHighScore(); 
        } catch (FileNotFoundException e) {
            e.printStackTrace(); 
        }
        // Проверка, установлен ли новый рекорд.
        if (score > highScore) {
            wasNewHighScore = true; 
            try {
                setHighScore(score); 
            } catch (IOException e) {
                e.printStackTrace(); 
            }
            highScore = score; 
        } else {
            wasNewHighScore = false; 
        }
    }

    // Отрисовка сцены
    public void render(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, null); 
        super.render(g);
        InputManager inputManager = Game.getInstance().getInputManager(); 
        g.setColor(Color.WHITE); 
        g.setFont(DEATH_FONT); 

        String deathMessage = "ВЫ ПОГИБЛИ";
        int containerWidth = Main.WIDTH; // Ширина контейнера.
        int deathMessageWidth = g.getFontMetrics().stringWidth(deathMessage); 
        int deathMessageX = (containerWidth - deathMessageWidth) / 2; // Вычисление координаты X для центрирования
        g.drawString(deathMessage, deathMessageX, 60);

        g.setFont(SCORE_FONT); 
        String scoreText = "Ваш счёт: " + score;
        int scoreTextWidth = g.getFontMetrics().stringWidth(scoreText); 
        int scoreTextX = (containerWidth - scoreTextWidth) / 2; // Вычисление координаты X для центрирования
        g.drawString(scoreText, scoreTextX, 130); 

        // Проверка, был ли установлен новый рекорд.
        if (wasNewHighScore) {
            String highScoreText = "Новый рекорд!"; 
            int highScoreTextWidth = g.getFontMetrics().stringWidth(highScoreText); 
            int highScoreTextX = (containerWidth - highScoreTextWidth) / 2; // Вычисление координаты X для центрирования
            g.drawString(highScoreText, highScoreTextX, 200); 
        } else {
            String highScoreText = "Ваш рекорд: " + highScore; 
            int highScoreTextWidth = g.getFontMetrics().stringWidth(highScoreText); 
            int highScoreTextX = (containerWidth - highScoreTextWidth) / 2; // Вычисление координаты X для центрирования
            g.drawString(highScoreText, highScoreTextX, 200); 
        }

        g.setFont(UI_FONT); 
        currentOption = upDown(inputManager, ENDING_OPTIONS, currentOption); // Обработка перемещения по меню
        renderScrollingMenus(g, ENDING_OPTIONS, currentOption); // Отрисовка меню
        endMenuEnter(inputManager); // Обработка нажатия клавиш для выбора варианта
    }


    public void endMenuEnter(InputManager inputManager) {
        if (inputManager.getKeyDown(KeyEvent.VK_ENTER)) { 
            if (currentOption == 0) {
                Game.getInstance().loadScene(new GameScene()); // Перезапуск игры
            } else if (currentOption == 1) {
                Game.getInstance().loadScene(new MainMenuScene("Main")); // Переход в главное меню
            } else if (currentOption == 2) {
                Game.getInstance().stop(); // Выход из игры
            }
        }
    }

    // Получение рекордного счета из файла
    public int getHighScore() throws FileNotFoundException {
        File highScoreFile; 
        int highScore = 0; 

        highScoreFile = new File("highScore.txt"); 

        if (highScoreFile.exists()) { 
            Scanner scanner = new Scanner(highScoreFile);
            if (scanner.hasNext()) {
                highScore = scanner.nextInt(); 
            }
            scanner.close(); // Закрытие сканера
        }
        return highScore; 
    }


    public void setHighScore(int highScore) throws IOException {
        File highScoreFile = new File("highScore.txt"); 

        if (!highScoreFile.exists()) { // Если файл не существует
            highScoreFile.createNewFile(); // Создание нового файла
        }

        FileWriter fw = new FileWriter(highScoreFile); // Создание FileWriter для записи в файл
        String highScoreString = "" + highScore; // Преобразуем в строку
        fw.write(highScoreString); // Запись рекордного счета в файл
        fw.close();
    }


    @Override
    public void dispose() {
        super.dispose();
        backgroundMusic.stop();
    }
}
