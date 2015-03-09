package se.dexica;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by dexter on 04/03/15.
 */
public class EventDelegator implements Runnable {
    private final List<ElevatorController> elevatorControllers;
    private Connector connector;
    private WorkOptimizer workOptimizer;
    private List<BlockingQueue<Runnable>> executors;

    public EventDelegator(List<ElevatorController> elevatorControllers, Connector connector, WorkOptimizer workOptimizer,
                          List<BlockingQueue<Runnable>> executors) {
        this.elevatorControllers = elevatorControllers;
        this.connector = connector;
        this.workOptimizer = workOptimizer;
        this.executors = executors;
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
            executors.get(Integer.parseInt(eventComponents[1]) - 1).put(new DelegatePanelEvent(eventComponents[1], eventComponents[2]));
        } else if (commandType.equals("b")) {
            delegateFloorButtonEvent(eventComponents[1], eventComponents[2]);
        } else if (commandType.equals("f")) {
            executors.get(Integer.parseInt(eventComponents[1]) - 1).put(new DelegatePositionEvent(eventComponents[1], eventComponents[2]));
        } else if (commandType.equals("v")) {
            delegateVelocityEvent(eventComponents[1]);
        }
    }

    private class DelegatePanelEvent implements Runnable {
        private int elevator;
        private int floor;

        public DelegatePanelEvent(String elevatorString, String floorString) {
            this.elevator = Integer.parseInt(elevatorString) - 1;
            this. floor = Integer.parseInt(floorString);
        }

        @Override
        public void run() {
            try {
                elevatorControllers.get(elevator).registerFloorRequest(new FloorButtonRequest(floor, Direction.NONE));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    private class DelegatePositionEvent implements Runnable{
        int elevator;
        float position;

        public DelegatePositionEvent(String elevatorString, String positionString) {
            this.elevator = Integer.parseInt(elevatorString) - 1;
            this.position = Float.parseFloat(positionString);
        }

        @Override
        public void run() {
            try {
                elevatorControllers.get(elevator).updatePosition(position);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
