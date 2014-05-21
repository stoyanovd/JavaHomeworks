package ru.ifmo.ctddev.stoyanov.task7;

import java.util.concurrent.BlockingQueue;

public class Worker implements Runnable {

    private static int TIME_TO_SLEEP_MILLIS = 2000;

    private final BlockingQueue<Task> producedQueue;
    private final BlockingQueue<Task> madeQueue;

    Worker(BlockingQueue<Task> producedQueue, BlockingQueue<Task> madeQueue) {
        this.producedQueue = producedQueue;
        this.madeQueue = madeQueue;
    }


    @Override
    public void run() {
        Task task = producedQueue.poll();
        task.run();
        madeQueue.add(task);
    }
}
