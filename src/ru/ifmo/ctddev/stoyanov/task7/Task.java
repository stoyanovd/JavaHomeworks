package ru.ifmo.ctddev.stoyanov.task7;

import java.util.concurrent.atomic.AtomicInteger;

public class Task implements Runnable {

    public long timeTosleep;
    public int id;
    public static AtomicInteger allIds = new AtomicInteger(1);
    public String threadNumber;

    Task(long time) {
        this.timeTosleep = time;
        this.threadNumber = Thread.currentThread().getName();
        this.id = allIds.getAndIncrement();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(timeTosleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
