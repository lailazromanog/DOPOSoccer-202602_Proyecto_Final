package domain;

public class Timer {
    private int totalTime;
    private double  currentTime;
    private boolean running;

    public Timer() {
        this(60);

    }
    public Timer(int seconds){
        this.totalTime = seconds;
        this.currentTime = seconds;
    }
    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void update(double deltaTime) {
        if (running) {
            currentTime -= deltaTime;
            if (currentTime < 0) {
                currentTime = 0;
            }
        }
    }

    public boolean isTimeUp() {
        return currentTime <= 0;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getCurrentTime() {
        return (int) Math.ceil(currentTime);
    }

    public boolean isRunning() {
        return running;
    }
}
