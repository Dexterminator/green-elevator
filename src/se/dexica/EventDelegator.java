package se.dexica;

import java.io.IOException;
import java.util.List;

/**
 * Created by dexter on 04/03/15.
 */
public class EventDelegator implements Runnable {
    private final List<ElevatorController> elevatorControllers;
    private Connector connector;
    private WorkOptimizer workOptimizer;

    public EventDelegator(List<ElevatorController> elevatorControllers, Connector connector, WorkOptimizer workOptimizer) {
        this.elevatorControllers = elevatorControllers;
        this.connector = connector;
        this.workOptimizer = workOptimizer;
    }

    private void readEvents() throws IOException, InterruptedException {
        String line;
        while ((line = connector.readLine()) != null) {
            parseEvent(line);
        }
    }

    private void parseEvent(String event) throws InterruptedException {
        String[] eventComponents = event.split(" ");
        String commandType = eventComponents[0];
        if (commandType.equals("p")) {
            delegatePanelEvent(eventComponents[1], eventComponents[2]);
        } else if (commandType.equals("b")) {
            delegateFloorButtonEvent(eventComponents[1], eventComponents[2]);
        } else if (commandType.equals("f")) {
            delegatePositionEvent(eventComponents[1], eventComponents[2]);
        } else if (commandType.equals("v")) {
            delegateVelocityEvent(eventComponents[1]);
        }
    }

    private void delegatePanelEvent(String elevatorString, String floorString) throws InterruptedException {
        int elevator = Integer.parseInt(elevatorString) - 1;
        int floor = Integer.parseInt(floorString);
        elevatorControllers.get(elevator).registerFloorRequest(new FloorButtonRequest(floor, Direction.NONE));
    }

    private void delegateFloorButtonEvent(String floorString, String directionString) throws InterruptedException {
        int floor = Integer.parseInt(floorString);
        int directionInt = Integer.parseInt(directionString);
        Direction direction;
        switch (directionInt) {
            case 1: direction = Direction.UP;
                break;
            case -1: direction = Direction.DOWN;
                break;
            default: throw new IllegalArgumentException("wat");
        }
        workOptimizer.registerFloorButtonRequest(floor, direction);
    }

    private void delegatePositionEvent(String elevatorString, String positionString) throws InterruptedException {
        int elevator = Integer.parseInt(elevatorString) - 1;
        float position = Float.parseFloat(positionString);
        elevatorControllers.get(elevator).updatePosition(position);
    }

    private void delegateVelocityEvent(String velocity) {

    }

    @Override
    public void run() {
        try {
            readEvents();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
