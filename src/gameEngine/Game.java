package gameEngine; 

/*  
    Класс, описывающий основной цикл игры, где мы обновляем состояния всех объектов в потоке
*/

import java.awt.Graphics;
import java.awt.Image; 
import java.awt.image.BufferStrategy; 

public class Game { 
    private static Game instance; // Тут храним единственный экземпляр игры

    private String title; 
    private int width; 
    private int height; 
    private InputManager inputManager; 
    private Display display; 
    private Scene currentScene; // Текущая сцена
    private Scene nextScene; // Сцена, ожидающая загрузки
    private boolean running; 
    private double timeScale; // масштаб времени для управления скоростью игры
    private double elapsedTimeSeconds; // общее время, прошедшее с начала игры
    private double deltaTimeSeconds; // время, прошедшее с последнего обновления


    public Game(String title, int width, int height, Image icon) {
        if (instance != null) // Если экземпляр игры уже существует
        { 
            throw new IllegalStateException("Экземпляр Game уже существует"); 
        }
        this.title = title; 
        this.width = width;
        this.height = height;
        inputManager = new InputManager(); 
        display = new Display(title, width, height, icon, inputManager.getKeyListener()); 
        currentScene = null; 
        nextScene = null; 
        running = false; 
        timeScale = 1.0; // Установка масштаба времени по умолчанию.
        elapsedTimeSeconds = 0.0; // Изначально прошедшее время = 0.
        deltaTimeSeconds = 0.0; // Изначально время обновления = 0.
        instance = this; // Установка текущего экземпляра как единственного.
    }

    public static Game getInstance() { 
        return instance;
    }

    public String getTitle() { 
        return title; 
    }

    public int getWidth() { 
        return width; 
    }

    public int getHeight() { 
        return height;
    }

    public InputManager getInputManager() { 
        return inputManager;
    }

    public Display getDisplay() { 
        return display;
    }


    // Запуск игры
    public void start() 
    { 
        if (running)
        { 
            throw new IllegalStateException("Игра уже запущена");
        }

        display.open();
        running = true;
        
        new Thread() { // Поток для выполнения gameloop'а
            @Override
            public void run() // Метод для запуска игрового цикла (переопределение)
            { 
                long lastTimeMilis = System.currentTimeMillis(); // Запись текущего времени в миллисекундах
                while (running) // Пока игра запущена
                {
                    long currentTimeMilis = System.currentTimeMillis(); // Получение текущего времени
                    long absoluteDeltaTimeMilis = currentTimeMilis - lastTimeMilis; // Вычисление времени, прошедшего с последнего обновления
                    lastTimeMilis = currentTimeMilis; // Обновление времени последнего обновления
                    deltaTimeSeconds = absoluteDeltaTimeMilis * 0.001 * timeScale; // Вычисление времени обновления в секундах с учетом масштаба времени
                    elapsedTimeSeconds += deltaTimeSeconds; // Обновление общего времени игры
                    
                    if (nextScene != null)  // Если есть следующая сцена для загрузки
                    {
                        if (currentScene != null)  // Если текущая сцена существует
                        {
                            currentScene.dispose(); // освободить ее ресурсы
                        }
                        currentScene = nextScene; 
                        nextScene = null; 
                        currentScene.initialize(); 
                    }
                    if (currentScene != null) // Если текущая сцена существует
                    { 
                        tick(); // Обновить состояние сцены
                        render(); // Отобразить сцену
                    }
                    inputManager.tick(); // Обновление состояния ввода
                }
                if (currentScene != null) // Если текущая сцена существует после выхода из цикла
                { 
                    currentScene.dispose(); // освободить ее ресурсы
                }
                display.close(); 
            }
        }.start(); // Запуск нового потока
    }

    public void stop() { // Остановка игры
        if (!running) { 
            throw new IllegalStateException("Игра уже остановлена"); 
        }
        running = false;
    }

    public Scene getOpenScene() { // Получения текущей открытой сцены
        return currentScene; 
    }

    public void loadScene(Scene scene) { // Загрузка новой сцены
        nextScene = scene; 
    }

    private void tick() { // Обновления состояния текущей сцены
        currentScene.tick(); 
    }

    private void render() { // Отрисовка текущей сцены
        BufferStrategy bufferStrategy = display.getBufferStrategy(); 
        Graphics graphics = bufferStrategy.getDrawGraphics(); 
        graphics.clearRect(0, 0, width, height); 
        currentScene.render(graphics); 
        graphics.dispose(); 
        bufferStrategy.show(); // Отображение обновленного содержимого
    }

    public double getTimeScale() { // Получение масштаба времени
        return timeScale; 
    }

    public void setTimeScale(double timeScale) { // Установка нового масштаба времени
        this.timeScale = timeScale; 
    }

    public double getTime() { // Получение общего времени игры
        return elapsedTimeSeconds; 
    }

    public double getDeltaTime() { // Получение времени обновления
        return deltaTimeSeconds; 
    }
}