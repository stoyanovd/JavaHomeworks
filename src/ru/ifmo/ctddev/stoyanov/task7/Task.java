package ru.ifmo.ctddev.stoyanov.task7;

public class Task implements Runnable {

    public long timeTosleep;

    Task(long time) {
        this.timeTosleep = time;
    }

    @Override
    public void run() {
        try {
            wait(timeTosleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
