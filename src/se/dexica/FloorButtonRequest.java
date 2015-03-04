package se.dexica;

/**
 * Created by dexter on 04/03/15.
 */
public class FloorButtonRequest {
    public final int floor;
    public final FloorButtonRequest.direction requestedDirection;
    public enum direction {UP, DOWN};

    public FloorButtonRequest(int floor, direction requestedDirection) {
        this.floor = floor;
        this.requestedDirection = requestedDirection;
    }
}
