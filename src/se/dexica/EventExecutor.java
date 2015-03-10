package se.dexica;

import java.util.concurrent.BlockingQueue;

/**
 * Created by dexter on 09/03/15.
 */
public class EventExecutor implements Runnable {
    private BlockingQueue<Runnable> events;

    public EventExecutor(BlockingQueue<Runnable> events) {
        this.events = events;
    }

    @Override
    public void run() {
        while (true) {
            try {
                events.take().run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
