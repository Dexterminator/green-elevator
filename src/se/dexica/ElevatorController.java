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
    private Direction direction = Direction.NONE;
    private BlockingQueue<Integer> floorRequests = new ArrayBlockingQueue<Integer>(2000);
    private List<Integer> stashedRequests = new ArrayList<Integer>();
    private List<Integer> currentPath = new ArrayList<Integer>();
    private int destination;

    public ElevatorController(int id, Connector connector) {
        this.id = id;
        this.connector = connector;
    }

    public synchronized void registerFloorRequest(int floor) throws InterruptedException {
        System.out.println("Added new request to list");
        floorRequests.put(floor);
        System.out.println("Floor request list size: " + floorRequests.size());
    }

    public synchronized void updatePosition(float newPosition) {
        position = newPosition;
    }

    public synchronized float getPosition() {
        return position;
    }

    public synchronized Direction getDirection() {
        return direction;
    }

    public synchronized int getDestination() {
        return destination;
    }

    private void move() throws InterruptedException {
        //TODO: Handle this in updatePosition instead!
        if (getPosition() > destination) {
            direction = Direction.DOWN;
        } else {
            direction = Direction.UP;
        }
        String output = "m " + id + " " + (direction == Direction.UP ? 1 : -1);
        System.out.println("Moving command made: " + output);
        connector.printLine(output);
        float abs = Math.abs(getPosition() - (float) destination);
        while (abs < 0.05) {
            abs = Math.abs(getPosition() - (float) destination);
        }
        System.out.println("Arriving at destination, yeah!");
        connector.printLine("m " + id + " " + 0);
        connector.printLine("d " + id + " " + 1);
        Thread.sleep(1000);
        connector.printLine("d " + id + " " + -1);
        Thread.sleep(1000);
        direction = Direction.NONE;
    }

    @Override
    public void run() {
        while (true) {
            try {
                serveRequests();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void serveRequests() throws InterruptedException {
        int newRequest = floorRequests.take();
        if (direction == Direction.NONE) {
            destination = newRequest;
        } else if (direction == Direction.UP) {
            if (newRequest < destination && newRequest - getPosition() > 0.05) {
                currentPath.add(destination);
                destination = newRequest;
            } else if (newRequest > destination) {
                currentPath.add(newRequest);
            } else {
                stashedRequests.add(newRequest);
            }
        } else if (direction == Direction.DOWN) {
            if (newRequest > destination && newRequest - getPosition() < -0.05) {
                currentPath.add(destination);
                destination = newRequest;
            } else if (newRequest < destination) {
                currentPath.add(newRequest);
            } else {
                stashedRequests.add(newRequest);
            }

        }
        System.out.println("Destination retrieved making a move");
        move();
    }
}
