package se.dexica;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by dexter on 04/03/15.
 */
public class ElevatorController implements Runnable {
    private Connector connector;
    private final int id;
    private float position = 0.0f;
    private int direction = 0;
    List<Integer> floorRequests = new ArrayList<Integer>();
//    BlockingQueue floorRequests = new ArrayBlockingQueue<Integer>();

    public ElevatorController(int id, Connector connector) {
        this.id = id;
        this.connector = connector;
    }

    public synchronized void registerFloorRequest(int floor) {
        System.out.println("Added new request to list");
        floorRequests.add(floor);
        System.out.println("Floor request list size: " + floorRequests.size());
    }

    public synchronized void updatePosition(float newPosition) {
        position = newPosition;
    }

    public synchronized float getPosition() {
        return position;
    }

    private void move(int destination) {
        if (position > destination) {
            direction = -1;
        } else {
            direction = 1;
        }
        String output = "m " + id + " " + direction;
        System.out.println("Moving command made" + output);
        connector.printLine(output);
        while (true) {
            if (position == destination) {
                System.out.println("Arriving at destination, yeah!");
                break;
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            if (!floorRequests.isEmpty()) {
                int destination = floorRequests.remove(floorRequests.size());
                System.out.println("Destination retrieved making a move");
                move(destination);
            }
        }
    }
}
