package se.dexica;

/**
 * Created by dexter on 04/03/15.
 */
public class FloorRequest implements Comparable<FloorRequest>{
    public final int floor;
    public final Direction direction;

    public FloorRequest(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }

    @Override
    public int compareTo(FloorRequest other) {
        return floor - other.floor;
    }

    @Override
    public String toString() {
        return "FloorRequest{" +
                "floor=" + floor +
                ", direction=" + direction +
                '}';
    }
}
