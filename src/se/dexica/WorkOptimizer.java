package se.dexica;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by dexter on 04/03/15.
 */
public class WorkOptimizer implements Runnable{
    private List<ElevatorController> elevatorControllers;
    private Connector connector;
    private BlockingQueue<FloorButtonRequest> floorButtonRequests = new ArrayBlockingQueue<FloorButtonRequest>(2000);

    public WorkOptimizer(List<ElevatorController> elevatorControllers, Connector connector) {
        this.elevatorControllers = elevatorControllers;
        this.connector = connector;
    }

    public synchronized void registerFloorButtonRequest(int floor, FloorButtonRequest.direction direction) {
        floorButtonRequests.add(new FloorButtonRequest(floor, direction));
    }

    @Override
    public void run() {
        while (true) {
            try {
                FloorButtonRequest request = floorButtonRequests.take();
                elevatorControllers.get(0).registerFloorRequest(request.floor);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
