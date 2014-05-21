package ru.ifmo.ctddev.stoyanov.task7;

import java.util.concurrent.BlockingQueue;

public class Publisher implements Runnable {

    private BlockingQueue<Task> madeQueue;

    Publisher(BlockingQueue madeQueue) {
        this.madeQueue = madeQueue;
    }

    @Override
    public void run() {
        Task task = madeQueue.poll();
        System.out.println("Task completed: sleeped for " + task.timeTosleep + " millis.");
    }
}
