package se.dexica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int numberOfElevators = 3;

    public static void main(String[] args) throws IOException {
        List<ElevatorController> elevatorControllers = new ArrayList<ElevatorController>();
        for (int i = 0; i < numberOfElevators; i++) {
            ElevatorController elevatorController = new ElevatorController(i + 1);
            elevatorControllers.add(elevatorController);
            new Thread(elevatorController).start();
        }
        new Thread(new EventDelegator(elevatorControllers)).start();
    }
}
