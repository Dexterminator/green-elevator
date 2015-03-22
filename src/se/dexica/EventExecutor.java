package se.dexica;

import java.util.concurrent.BlockingQueue;

/**
 * Executes the events from the EventDelegator for one of the elevators, making sure that this
 * is done on separate threads.
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
