package se.dexica;

/**
 * Created by dexter on 04/03/15.
 */
public class FloorButtonRequest implements Comparable<FloorButtonRequest>{
    public final int floor;
    public final Direction direction;

    public FloorButtonRequest(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }

    @Override
    public int compareTo(FloorButtonRequest other) {
        return floor - other.floor;
    }

    @Override
    public String toString() {
        return "FloorButtonRequest{" +
                "floor=" + floor +
                ", direction=" + direction +
                '}';
    }
}
