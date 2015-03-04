package se.dexica;

import java.io.IOException;
import java.util.List;

/**
 * Created by dexter on 04/03/15.
 */
public class EventDelegator implements Runnable {
    private final List<ElevatorController> elevatorControllers;

    public EventDelegator(List<ElevatorController> elevatorControllers) {
        this.elevatorControllers = elevatorControllers;
    }

    private void readEvents() throws IOException {
        Connector connector = new Connector();
        connector.connect();
        String line;
        while ((line = connector.readLine()) != null) {
            System.out.println(line);
        }
    }

    private void parseEvent(String event) {
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

    private void delegatePanelEvent(String elevatorString, String floorString) {
        int elevator = Integer.parseInt(elevatorString);
        int floor = Integer.parseInt(floorString);
        elevatorControllers.get(elevator).registerFloorRequest(floor);
    }

    private void delegateFloorButtonEvent(String floorString, String directionString) {

    }

    private void delegatePositionEvent(String elevatorString, String positionString) {
        int elevator = Integer.parseInt(elevatorString);
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
        }
    }
}
