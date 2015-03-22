package se.dexica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    private static int numberOfElevators = 3;

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            numberOfElevators = Integer.parseInt(args[0]);
        }
        Connector connector = new Connector();
        connector.connect();
        List<BlockingQueue<Runnable>> executors = new ArrayList<>();
        CommandSender commandSender = new CommandSender(connector);
        List<ElevatorController> elevatorControllers = new ArrayList<>();
        for (int i = 0; i < numberOfElevators; i++) {
            ElevatorController elevatorController = new ElevatorController(i+1, commandSender);
            elevatorControllers.add(elevatorController);
            ArrayBlockingQueue<Runnable> eventQueue = new ArrayBlockingQueue<>(2000);
            executors.add(eventQueue);
            new Thread(elevatorController).start();
            new Thread(new EventExecutor(eventQueue)).start();
        }
        WorkOptimizer workOptimizer = new WorkOptimizer(elevatorControllers);
        new Thread(new EventDelegator(elevatorControllers, connector, workOptimizer, executors)).start();
        new Thread(workOptimizer).start();
    }
}
