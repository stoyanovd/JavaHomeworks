package ru.ifmo.ctddev.stoyanov.task7;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainClass {

    private static int MAX_RUNNABLES = 30;
    private static int MAX_PRODUCERS = 7;
    private static int MAX_WORKERS = 7;
    private static int MAX_PUBLISHERS = 7;
    private static int MAX_THREADS = 21;


    static BlockingQueue<Runnable> runnables;
    static BlockingQueue<Task> producedQueue;
    static BlockingQueue<Task> madeQueue;

    public static void main(String[] arg) {

        runnables = new LinkedBlockingQueue<>();

        for (int i = 0; i < MAX_PRODUCERS; i++) {
            runnables.add(new Producer(producedQueue));
        }
        for (int i = 0; i < MAX_WORKERS; i++) {
            runnables.add(new Worker(producedQueue, madeQueue));
        }
        for (int i = 0; i < MAX_PUBLISHERS; i++) {
            runnables.add(new Publisher(madeQueue));
        }

        for (int i = 0; i < MAX_THREADS; i++) {
            (new Thread(new RunnablesRunner(runnables))).start();
        }


    }
}
