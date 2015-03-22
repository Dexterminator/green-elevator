package se.dexica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Keeps track of the state of one of the elevators. Services requests in one direction at a time, using
 * the upPath and the downPath to keep track of the requests. Automatically checks if the current destination
 * should be changed when a new requests come in, in order to optimize movement.
 */
public class ElevatorController implements Runnable {
    private CommandSender commandSender;
    private final int id;
    private volatile float position = 0.0f;
    private Direction direction = Direction.NONE;
    private Direction intendedDirection = Direction.NONE;

    private BlockingQueue<FloorRequest> floorRequests = new ArrayBlockingQueue<>(2000);
    private List<FloorRequest> upPath = new ArrayList<>();
    private List<FloorRequest> downPath = new ArrayList<>();
    private FloorRequest destination;

    public ElevatorController(int id, CommandSender commandSender) {
        this.id = id;
        this.commandSender = commandSender;
    }

    public synchronized void registerFloorRequest(FloorRequest floorRequest) throws InterruptedException {
        floorRequests.put(floorRequest);
    }

    public boolean isElevatorAtFloor(int floor) {
        return Math.abs(position - (float) floor) < 0.05;
    }

    public boolean isElevatorAboveFloor(int floor) {
        return (position - (float) floor) > 0.05;
    }

    public boolean isElevatorBelowFloor(int floor) {
        return (position - (float) floor) < -0.05;
    }

    public boolean isFloorBelow (int floor1, int floor2) {
        return floor1 - floor2 < 0;
    }

    public boolean isFloorAbove (int floor1, int floor2) {
        return floor1 - floor2 > 0;
    }

    public void sortUpPath() {
        Collections.sort(upPath, Collections.reverseOrder());
    }

    public void sortDownPath() {
        Collections.sort(downPath);
    }

    public boolean isNewTurnAroundGoal(FloorRequest otherRequest) {
        if (intendedDirection == Direction.UP && direction == Direction.DOWN && isFloorBelow(otherRequest.floor, destination.floor)) {
            return true;
        }
        if (intendedDirection == Direction.DOWN && direction == Direction.UP && isFloorAbove(otherRequest.floor, destination.floor)) {
            return true;
        }
        return false;
    }

    public boolean onWayToDestination(FloorRequest otherRequest) {
        if (intendedDirection == Direction.UP && isFloorBelow(otherRequest.floor, destination.floor)
                && isElevatorBelowFloor(otherRequest.floor) && direction == Direction.UP) {
            return true;
        }
        if (intendedDirection == Direction.DOWN && isFloorAbove(otherRequest.floor, destination.floor)
                && isElevatorAboveFloor(otherRequest.floor) && direction == Direction.DOWN) {
            return true;
        }
        return false;
    }

    public void stop() {
        commandSender.stop(id);
        upPath.clear();
        downPath.clear();
        floorRequests.clear();
        direction = Direction.NONE;
        intendedDirection = Direction.NONE;
    }

    public synchronized void updatePosition(float newPosition) throws InterruptedException {
        if (position == newPosition) {
            return;
        }

        position = newPosition;
        checkArrival();
    }

    private void checkArrival() throws InterruptedException {
        if (isElevatorAtFloor(destination.floor)) {
            stopAtFloor();
            boolean resolved = false;
            while (!resolved) {
                if (upPath.isEmpty() && downPath.isEmpty()) {
                    // No requests
                    resolved = resolveNoRequestsArrival();
                } else if (intendedDirection == Direction.UP) {
                    // We are currently trying to serve requests that are going up
                    resolved = resolveUpRequestsArrival(resolved);
                } else if (intendedDirection == Direction.DOWN) {
                    // We are currently trying to serve requests that are going down
                    resolved = resolveDownRequestsArrival(resolved);
                } else {
                    // Somehow, none of the above was matched. Should not happen.
                    System.out.println("No case matched, wat");
                }
            }
        }
    }

    private boolean resolveNoRequestsArrival() {
        boolean resolved;
        System.out.println("Up and down are empty! Direction set to none.");
        intendedDirection = Direction.NONE;
        direction = Direction.NONE;
        resolved = true;
        return resolved;
    }

    private boolean resolveDownRequestsArrival(boolean resolved) throws InterruptedException {
        if (!downPath.isEmpty()) {
            FloorRequest newDestination = downPath.remove(downPath.size() - 1);
            if (destination.floor == newDestination.floor) {
                stopAtFloor(); // Same floor, do not set as resolved
            } else {
                destination = newDestination;
                direction = isElevatorBelowFloor(destination.floor) ? Direction.UP : Direction.DOWN;
                commandSender.move(direction, id);
                resolved = true;
            }
        } else {
            System.out.println("DownPath empty! Time to switch intended direction!");
            intendedDirection = Direction.UP;
        }
        return resolved;
    }

    private boolean resolveUpRequestsArrival(boolean resolved) throws InterruptedException {
        if (!upPath.isEmpty()) {
            FloorRequest newDestination = upPath.remove(upPath.size() - 1);
            if (destination.floor == newDestination.floor) {
                stopAtFloor(); // Same floor, do not set as resolved
            } else {
                destination = newDestination;
                direction = isElevatorBelowFloor(destination.floor) ? Direction.UP : Direction.DOWN;
                commandSender.move(direction, id);
                resolved = true;
            }
        } else {
            System.out.println("UpPath empty! Time to switch intended direction!");
            intendedDirection = Direction.DOWN;
        }
        return resolved;
    }

    private void stopAtFloor() throws InterruptedException {
        System.out.println("Arrived at destination!");
        System.out.println("UpPath: " + upPath);
        System.out.println("DownPath: " + downPath);
        commandSender.stop(id);
        commandSender.openDoor(id);
        Thread.sleep(1000);
        commandSender.closeDoor(id);
        Thread.sleep(1000);
    }

    public synchronized float getPosition() {
        return position;
    }

    public synchronized Direction getDirection() {
        return direction;
    }

    public synchronized Direction getIntendedDirection() {
        return intendedDirection;
    }

    public synchronized FloorRequest getDestination() {
        return destination;
    }

    private void serveRequest(FloorRequest newRequest) throws InterruptedException {
        if (direction == Direction.NONE) {
            initiateMoveFromInactive(newRequest);
        } else if (newRequest.direction == Direction.UP) {
            handleUpRequest(newRequest);
        } else if (newRequest.direction == Direction.DOWN) {
            handleDownRequest(newRequest);
        } else if (newRequest.direction == Direction.NONE) {
            handlePanelRequest(newRequest);
        } else {
            System.out.println("No case matched by request");
        }
        System.out.println("new request: " + newRequest);
        System.out.println("uppath: " + upPath);
        System.out.println("downpath: " + downPath);
        System.out.println("destination: " + destination);
    }

    private void handlePanelRequest(FloorRequest newRequest) {
        System.out.println("Handling panel request");
        if (intendedDirection == Direction.UP && direction == Direction.UP) {
            if (onWayToDestination(newRequest)) {
                System.out.println("request " + newRequest + " is on the way to destination " + destination);
                upPath.add(destination);
                sortUpPath();
                destination = newRequest;
            } else if (isElevatorBelowFloor(newRequest.floor)) {
                upPath.add(newRequest);
                sortUpPath();
            } else if (isElevatorAboveFloor(newRequest.floor)) {
                downPath.add(newRequest);
                sortDownPath();
            } else {
                System.out.println("Direction and intended direction up. no case matched");
            }
        } else if (intendedDirection == Direction.DOWN && direction == Direction.DOWN) {
            if (onWayToDestination(newRequest)) {
                System.out.println("request " + newRequest + " is on the way to destination " + destination);
                downPath.add(destination);
                sortDownPath();
                destination = newRequest;
            } else if (isElevatorAboveFloor(newRequest.floor)) {
                downPath.add(newRequest);
                sortDownPath();
            } else if (isElevatorBelowFloor(newRequest.floor)) {
                upPath.add(newRequest);
                sortUpPath();
            } else {
                System.out.println("Direction and intended direction down. no case matched");
            }
        } else if ((intendedDirection == Direction.UP && direction == Direction.DOWN) ||
                (intendedDirection == Direction.DOWN && direction == Direction.UP)) {
            if (isFloorAbove(newRequest.floor, destination.floor)) {
                upPath.add(newRequest);
                sortUpPath();
            } else if (isFloorBelow(newRequest.floor, destination.floor)) {
                downPath.add(newRequest);
                sortDownPath();
            }
        } else {
            System.out.println("No case for this panel request!");
        }
    }

    private void handleDownRequest(FloorRequest newRequest) {
        System.out.println("Handling down request");
        if (onWayToDestination(newRequest) || isNewTurnAroundGoal(newRequest)) {
            downPath.add(destination);
            destination = newRequest;
        } else {
            downPath.add(newRequest);
        }
        sortDownPath();
    }

    private void handleUpRequest(FloorRequest newRequest) {
        System.out.println("Handling up request");
        if (onWayToDestination(newRequest) || isNewTurnAroundGoal(newRequest)) {
            upPath.add(destination);
            destination = newRequest;
        } else {
            upPath.add(newRequest);
        }
        sortUpPath();
    }

    private void initiateMoveFromInactive(FloorRequest newRequest) throws InterruptedException {
        if (isElevatorAtFloor(newRequest.floor)) {
            stopAtFloor();
        } else {
            direction = isElevatorBelowFloor(newRequest.floor) ? Direction.UP : Direction.DOWN;
            destination = newRequest;
            commandSender.move(direction, id);
            intendedDirection = destination.direction == Direction.NONE ? direction : destination.direction;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                FloorRequest newRequest = floorRequests.take();
                serveRequest(newRequest);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
