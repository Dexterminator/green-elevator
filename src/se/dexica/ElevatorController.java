package se.dexica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by dexter on 04/03/15.
 */
public class ElevatorController implements Runnable {
    private CommandSender commandSender;
    private final int id;
    private float position = 0.0f;
    private Direction direction = Direction.NONE;
    private BlockingQueue<FloorButtonRequest> floorRequests = new ArrayBlockingQueue<FloorButtonRequest>(2000);
    private List<Integer> stashedRequests = new ArrayList<Integer>();
    private List<Integer> currentPath = new ArrayList<Integer>();
    private int destination;
    private FloorButtonRequest currentRequest;

    public ElevatorController(int id, CommandSender commandSender) {
        this.id = id;
        this.commandSender = commandSender;
    }

    public synchronized void registerFloorRequest(FloorButtonRequest floorButtonRequest) throws InterruptedException {
        System.out.println("Added new request to list");
        if (floorButtonRequest.direction != direction && floorButtonRequest.direction != Direction.NONE && direction != Direction.NONE) {
            stashedRequests.add(floorButtonRequest.floor);
        } else {
            floorRequests.put(floorButtonRequest);
        }
        System.out.println("Floor request list size: " + floorRequests.size());
    }

    public synchronized void updatePosition(float newPosition) throws InterruptedException {
        if (position == newPosition)
            return;
        position = newPosition;
        float abs = Math.abs(position - (float) destination);
        if (abs < 0.05) {
            System.out.println("Arriving at destination, yeah!");
            commandSender.stop(id);
            commandSender.openDoor(id);
            Thread.sleep(1000);
            commandSender.closeDoor(id);
            Thread.sleep(1000);
            if (!currentPath.isEmpty()) {
                System.out.println("Stuff in current path");
                destination = currentPath.remove(currentPath.size()-1);
                commandSender.move(direction, id);
            } else if (!stashedRequests.isEmpty()) {
                System.out.println(stashedRequests);
                currentPath.addAll(stashedRequests);
                stashedRequests.clear();
                direction = direction == Direction.UP ? Direction.DOWN : Direction.UP;
                if (direction == Direction.UP) {
                    Collections.sort(currentPath, Collections.reverseOrder());
                } else {
                    Collections.sort(currentPath);
                }
                destination = currentPath.remove(currentPath.size()-1);
                commandSender.move(direction, id);
            } else {
                System.out.println("Path and stashed are empty");
                direction = Direction.NONE;
            }
        }
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

    @Override
    public void run() {
        while (true) {
            try {
                FloorButtonRequest newRequest = floorRequests.take();
                serveRequest(newRequest);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void serveRequest(FloorButtonRequest newRequest) throws InterruptedException {
        int newFloor = newRequest.floor;
        if (direction == Direction.NONE) {
            destination = newFloor;
            if (getPosition() > destination) {
                direction = Direction.DOWN;
            } else {
                direction = Direction.UP;
            }
            commandSender.move(direction, id);
        } else if (direction == Direction.UP) {
            if (newFloor < destination && newFloor - getPosition() > 0.05) {
                currentPath.add(destination);
                Collections.sort(currentPath, Collections.reverseOrder());
                System.out.println(currentPath);
                destination = newFloor;
            } else if (newFloor > destination) {
                currentPath.add(newFloor);
                Collections.sort(currentPath, Collections.reverseOrder());
                System.out.println(currentPath);
            } else {
                stashedRequests.add(newFloor);
            }
        } else if (direction == Direction.DOWN) {
            if (newFloor > destination && newFloor - getPosition() < -0.05) {
                currentPath.add(destination);
                Collections.sort(currentPath, Collections.reverseOrder());
                destination = newFloor;
            } else if (newFloor < destination) {
                currentPath.add(newFloor);
                Collections.sort(currentPath, Collections.reverseOrder());
            } else {
                stashedRequests.add(newFloor);
            }
        }

        System.out.println("Destination retrieved making a move");
    }
}
