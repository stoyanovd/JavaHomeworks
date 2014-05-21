package ru.ifmo.ctddev.stoyanov.task7;

import java.util.concurrent.BlockingQueue;

public class RunnablesRunner implements Runnable {

    private final BlockingQueue<Runnable> runnablesToRun;

    RunnablesRunner(BlockingQueue<Runnable> runnablesToRun) {
        this.runnablesToRun = runnablesToRun;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Runnable runnable = runnablesToRun.poll();
            if (runnable == null) {
                continue;
            }
            runnable.run();
            runnablesToRun.add(runnable);
        }

    }
}
