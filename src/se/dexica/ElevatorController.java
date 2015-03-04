package se.dexica;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by dexter on 04/03/15.
 */
public class ElevatorController implements Runnable {
    private final int id;
    private float position = 0.0f;
    List<Integer> floorRequests = new ArrayList<Integer>();
//    BlockingQueue floorRequests = new ArrayBlockingQueue<Integer>();

    public ElevatorController(int id) {
        this.id = id;
    }

    public synchronized void registerFloorRequest(int floor) {
        floorRequests.add(floor);
    }

    public synchronized void updatePosition(float newPosition) {
        position = newPosition;
    }

    public synchronized float getPosition() {
        return position;
    }

    @Override
    public void run() {
        while (true) {
            if (!floorRequests.isEmpty()) {
                int destination = floorRequests.remove(floorRequests.size());
            }
        }
    }
}
