package se.dexica;

import jdk.nashorn.internal.ir.Block;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    private static final int numberOfElevators = 2;

    public static void main(String[] args) throws IOException {
        Connector connector = new Connector();
        connector.connect();
        List<BlockingQueue<Runnable>> executors = new ArrayList<BlockingQueue<Runnable>>();
        CommandSender commandSender = new CommandSender(connector);
        List<ElevatorController> elevatorControllers = new ArrayList<ElevatorController>();
        for (int i = 0; i < numberOfElevators; i++) {
            ElevatorController elevatorController = new ElevatorController(i+1, commandSender);
            elevatorControllers.add(elevatorController);
            executors.add(new ArrayBlockingQueue<Runnable>(2000));
            new Thread(elevatorController).start();
        }
        WorkOptimizer workOptimizer = new WorkOptimizer(elevatorControllers);
        new Thread(new EventDelegator(elevatorControllers, connector, workOptimizer, executors)).start();
        new Thread(workOptimizer).start();
    }
}
