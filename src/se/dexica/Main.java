package se.dexica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int numberOfElevators = 2;

    public static void main(String[] args) throws IOException {
        Connector connector = new Connector();
        connector.connect();
        CommandSender commandSender = new CommandSender(connector);
        List<ElevatorController> elevatorControllers = new ArrayList<ElevatorController>();
        for (int i = 0; i < numberOfElevators; i++) {
            ElevatorController elevatorController = new ElevatorController(i+1, commandSender);
            elevatorControllers.add(elevatorController);
            new Thread(elevatorController).start();
        }
        WorkOptimizer workOptimizer = new WorkOptimizer(elevatorControllers);
        new Thread(new EventDelegator(elevatorControllers, connector, workOptimizer)).start();
        new Thread(workOptimizer).start();
    }
}
