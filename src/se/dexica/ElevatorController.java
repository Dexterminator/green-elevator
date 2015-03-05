package se.dexica;

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
    BlockingQueue<Integer> floorRequests = new ArrayBlockingQueue<Integer>(2000);

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

    private void move(int destination) throws InterruptedException {
        if (position > destination) {
            direction = Direction.DOWN;
        } else {
            direction = Direction.UP;
        }
        String output = "m " + id + " " + (direction == Direction.UP ? 1 : -1);
        System.out.println("Moving command made: " + output);
        connector.printLine(output);
        while (true) {
            float abs = Math.abs(getPosition() - (float) destination);
            if (abs < 0.05) {
                System.out.println("Arriving at destination, yeah!");
                connector.printLine("m " + id + " " + 0);
                connector.printLine("d " + id + " " + 1);
                Thread.sleep(1000);
                connector.printLine("d " + id + " " + -1);
                Thread.sleep(1000);
                direction = Direction.NONE;
                break;
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                int destination = floorRequests.take();
                System.out.println("Destination retrieved making a move");
                move(destination);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
