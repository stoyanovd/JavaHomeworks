package ru.ifmo.ctddev.stoyanov.task7;

import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {

    private static long TIME_TO_SLEEP_MILLIS = 2000;

    private final BlockingQueue<Task> producedQueue;

    Producer(BlockingQueue<Task> producedQueue) {
        this.producedQueue = producedQueue;
    }


    @Override
    public void run() {
        Task task = new Task(TIME_TO_SLEEP_MILLIS);
        try {
            producedQueue.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
