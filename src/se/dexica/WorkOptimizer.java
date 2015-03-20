package se.dexica;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by dexter on 04/03/15.
 */
public class WorkOptimizer implements Runnable{
    private List<ElevatorController> elevatorControllers;
    private BlockingQueue<FloorRequest> floorRequests = new ArrayBlockingQueue<FloorRequest>(2000);

    public WorkOptimizer(List<ElevatorController> elevatorControllers) {
        this.elevatorControllers = elevatorControllers;
    }

    public synchronized void registerFloorButtonRequest(int floor, Direction direction)
        throws InterruptedException {
        floorRequests.put(new FloorRequest(floor, direction));
    }

    public int score(FloorRequest request, ElevatorController elevatorController) {
        int score = 0;
        float distance = (float) request.floor - elevatorController.getPosition();
        if (elevatorController.getDirection() == Direction.NONE) {
            float abs = Math.abs(distance);
            if (abs < 0.05) {
                score += 150;
            } else if (abs < 1.05) {
                score += 100;
            } else if (abs < 2.05) {
                score += 75;
            } else if (abs < 3.05) {
                score += 50;
            } else if (abs < 4.05) {
                score += 25;
            } else if (abs < 5.05) {
                score += 10;
            }
        }

        if (request.direction == elevatorController.getIntendedDirection() && request.direction == elevatorController.getDestination().direction){
            if (request.direction == Direction.UP && distance > 0.05) {
                score += 100;
            }

            if (request.direction == Direction.DOWN && distance < -0.05) {
                score += 100;
            }
        }

        System.out.println("score: " + score);
        return score;
    }

    public ElevatorController getBestElevator(FloorRequest request) {
        ElevatorController bestElevator = null;
        int bestScore = Integer.MIN_VALUE;
        for (ElevatorController elevatorController : elevatorControllers) {
            int score = score(request, elevatorController);
            if (score > bestScore){
                bestElevator = elevatorController;
                bestScore = score;
            }
        }
        System.out.println("best score: " + bestScore);
        return bestElevator;
    }

    @Override
    public void run() {
        while (true) {
            try {
                FloorRequest request = floorRequests.take();
                getBestElevator(request).registerFloorRequest(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
