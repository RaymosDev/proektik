package impl.scenes; 

/* 
    Основная игровая сцена. Тут взаимодействия игрока, рисовка интерфейса, обработка событий, обновление состояния игры...
*/

import gameEngine.Collider;
import gameEngine.Entity;
import gameEngine.Entity.EntityCollider; 
import gameEngine.Game;
import gameEngine.InputManager;
import gameEngine.ResourceLoader;
import gameEngine.SceneObject;
import gameEngine.Vector2;
import impl.Main; 
import impl.entities.PlayerShip;
import impl.waves.Wave1;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList; 
import java.util.List;
import javax.sound.sampled.Clip; 

public class GameScene extends SceneWithKeys {
    public static final double FIRST_WAVE_WAIT_TIME = 2.5; // Время ожидания перед первой волной
    public static final double WAVE_REST_TIME = 5.0; // Время отдыха между волнами
    private static final Font UI_FONT = ResourceLoader.loadFont("res/Font.ttf", 50); 
    private static final Vector2 PLAYER_START = new Vector2(250, Main.HEIGHT / 2); // Начальная позиция игрока
    private final String[] PAUSE_MENU_OPTIONS = { "Продолжить", "Главное меню" };

    // Загрузка изображений для индикаторов здоровья
    private static final Image GREEN_HP = ResourceLoader.loadImage("res/images/ui/GreenHp.png")
            .getScaledInstance((int) (10 * 2.5), (int) (14 * 2.5), 0);
    private static final Image BLACK_HP = ResourceLoader.loadImage("res/images/ui/Black.png")
            .getScaledInstance((int) (10 * 2.5), (int) (14 * 2.5), 0);
    private static final Image END_HP = ResourceLoader.loadImage("res/images/ui/EndHp.png")
            .getScaledInstance((int) (10 * 2.5), (int) (14 * 2.5), 0);
    private static final Image YELLOW_HP = ResourceLoader.loadImage("res/images/ui/YellowHp.png")
            .getScaledInstance((int) (10 * 2.5), (int) (14 * 2.5), 0);
    private static final Image RED_HP = ResourceLoader.loadImage("res/images/ui/RedHp.png")
            .getScaledInstance((int) (10 * 2.5), (int) (14 * 2.5), 0);

    private Collider bounds; // Коллайдер для границ сцены
    private BufferedImage backgroundImage;
    private Clip backgroundMusic; 
    private PlayerShip player; 
    private int score; 
    private int currentPauseOption; // Текущая опция в меню паузы
    private boolean paused; 
    private InputManager inputManager; 

    private String waveMessage = ""; // Сообщение о волне
    private double waveMessageDuration = 0; // Длительность отображения сообщения о волне


    @Override
    public void initialize() {
        // Инициализация границ коллайдера
        bounds = new Collider(-500, -500, Main.WIDTH + 500, Main.HEIGHT + 500) {
            @Override
            public void onCollisionEnter(Collider other) {
            }

            @Override
            public void onCollisionExit(Collider other) { // Логика при выходе из коллайдера (из сцены по сути)
                if (other instanceof EntityCollider) {
                    Entity entity = ((EntityCollider) other).getEntity(); 
                    Game.getInstance().getOpenScene().removeObject(entity); // Удаление сущности из сцены
                }
            }
        };
        bounds.setActive(true); // Активация коллайдера
        

        backgroundImage = ResourceLoader.toBufferedImage(
                ResourceLoader.loadImage("res/images/backgrounds/GameBackground.png").getScaledInstance(24750, 825, 0));
        
        backgroundMusic = ResourceLoader.loadAudioClip("res/audio/GameMusic.wav");
        backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        
        currentPauseOption = 0; 
        inputManager = Game.getInstance().getInputManager();
        player = new PlayerShip(PLAYER_START); // Создание игрока на стартовой позиции
        addObject(player); // Добавление игрока в сцену
        addObject(new FadeIn(1.5)); 
        addObject(new Wave1(this));
    }


    @Override
    public void tick() {
        super.tick(); 
        
        // Сообщение о волне
        if (waveMessageDuration > 0) {
            waveMessageDuration -= Game.getInstance().getDeltaTime(); // Уменьшение времени отображения сообщения
            if (waveMessageDuration <= 0) {
                waveMessage = ""; // Сброс сообщения, если время истекло.
            }
        }


        if (paused) {
            if (Game.getInstance().getInputManager().getKeyDown(KeyEvent.VK_ESCAPE)) {
                unPause(); 
            }
            pauseTick(); 
        } else {
            if (Game.getInstance().getInputManager().getKeyDown(KeyEvent.VK_ESCAPE)) {
                pause(); 
            }
        }
    }

    // Обработка логики паузы
    private void pauseTick() {
        currentPauseOption = upDown(inputManager, PAUSE_MENU_OPTIONS, currentPauseOption); // Обработка выбора в меню паузы.
        if (inputManager.getKeyDown(KeyEvent.VK_ENTER)) {
            if (currentPauseOption == 0) {
                unPause(); 
            } else if (currentPauseOption == 1) {
                unPause(); 
                backgroundMusic.stop(); 
                Game.getInstance().loadScene(new MainMenuScene("Main")); // Переход в главное меню.
            }
        }
    }


    public boolean isPaused() {
        return paused;
    }

    // Установка паузы
    private void pause() {
        Game.getInstance().setTimeScale(0.0); // Остановка времени в игре
        ResourceLoader.loadAudioClip("res/audio/Pause.wav").start(); 
        paused = true; 
    }

    // Снятие паузы
    private void unPause() {
        Game.getInstance().setTimeScale(1.0); // Возобновление времени в игре.
        ResourceLoader.loadAudioClip("res/audio/Unpause.wav").start(); 
        paused = false;
    }

    // Отрисовка сцены
    @Override
    public void render(Graphics g) 
    {
        double time = Game.getInstance().getTime(); // Получение текущего времени игры
        int x = (int) (time * 150 % 22195); // Вычисление смещения для фона
        Image backgroundSubImage = backgroundImage.getSubimage(x, 0, Main.WIDTH, Main.HEIGHT); 
        g.drawImage(backgroundSubImage, 0, 0, null); 
        super.render(g); 
        
        g.setColor(Color.WHITE); 
        g.setFont(UI_FONT); 
        g.drawString("Счёт: " + score, 50, 70); // Отрисовка счёта

        
        if (!waveMessage.isEmpty()) // Отрисовка сообщения о волне, если оно не пустое.
        {
            g.drawString(waveMessage, Main.WIDTH / 2 - g.getFontMetrics().stringWidth(waveMessage) / 2, 100);
        }
        
        drawHealthBar(g); // Отрисовка индикатора здоровья
        
        // Если игра на паузе -. затемнённый экран и меню паузы.
        if (paused) {
            Color filter = new Color(0, 0, 0, 150);
            g.setColor(filter);
            g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT); 
            
            g.setColor(Color.WHITE); 
            String pauseMessage = "ПАУЗА";
            int containerWidth = Main.WIDTH; 
            int pauseMessageWidth = g.getFontMetrics().stringWidth(pauseMessage); 
            int pauseMessageX = (containerWidth - pauseMessageWidth) / 2; 
            g.drawString(pauseMessage, pauseMessageX, 230); 
            
            renderScrollingMenus(g, PAUSE_MENU_OPTIONS, currentPauseOption); 
        }
    }

    // Отрисовка индикатора здоровья игрока
    private void drawHealthBar(Graphics g) 
    {
        double healthProportion = player.getCurrentHealth() / player.getMaxHealth(); // Пропорция текущего здоровья
        int totalBlocks = (int) player.getMaxHealth(); // Общее количество блоков здоровья
        int numBars = (int) Math.ceil(healthProportion * totalBlocks); // Количество заполненных блоков
        int blockWidth = 23; // Ширина блока здоровья
        int padding = 10; // Отступ между блоками
        int xOffset = Main.WIDTH - (totalBlocks * blockWidth) - padding; // Смещение по оси X для индикатора здоровья

        g.drawImage(END_HP, xOffset - END_HP.getWidth(null), 35, null);

        // Отрисовка блоков здоровья
        for (int i = 0; i < totalBlocks; i++) 
        {
            Image hpBlock;
            if (i < numBars) { // Если блок заполнен.
                if (healthProportion < 0.33) {
                    hpBlock = RED_HP; // Красный блок
                } else if (healthProportion < 0.66) {
                    hpBlock = YELLOW_HP; // Желтый блок
                } else {
                    hpBlock = GREEN_HP; // Зеленый блок
                }
                g.drawImage(hpBlock, xOffset + i * blockWidth, 35, null); // Отрисовка заполненного блока
            } else {
                g.drawImage(BLACK_HP, xOffset + i * blockWidth, 35, null); // Отрисовка незаполненного блока
            }
        }
    }


    public PlayerShip getPlayer() {
        return player; 
    }


    public void addScore(int score) {
        this.score += score;
    }


    @Override
    public void dispose() {
        super.dispose(); 
        backgroundMusic.stop();

        List<SceneObject> objectsCopy = new ArrayList<>(objects); // Копирование объектов сцены.


        for (SceneObject object : objectsCopy) {
            object.dispose();
        }

        objects.clear();
        bounds.setActive(false); 
    }

    // Завершение игры
    public void endGame() {
        backgroundMusic.stop(); 
        Game.getInstance().loadScene(new EndingScene(score)); 
    }


    public int getScore() {
        return score;
    }

    // Установка сообщения о волне
    public void setWaveMessage(String message) {
        this.waveMessage = message; 
        this.waveMessageDuration = 3.0; 
    }
}
