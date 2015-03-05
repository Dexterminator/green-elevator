package se.dexica;

/**
 * Created by dexter on 04/03/15.
 */
public class FloorButtonRequest {
    public final int floor;
    public final Direction direction;

    public FloorButtonRequest(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }
}
