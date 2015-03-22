package se.dexica;

/**
 * Value class containing a floor and a direction.
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
