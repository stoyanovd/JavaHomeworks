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
        if (task == null) {
            return;
        }
        System.out.println("Task completed. Task id: " + task.id + "  and  thread number: " + task.threadNumber);
    }
}
