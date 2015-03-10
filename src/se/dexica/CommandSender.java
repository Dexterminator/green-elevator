package se.dexica;

/**
 * Created by dexter on 09/03/15.
 */
public class CommandSender {
    private Connector connector;

    public CommandSender(Connector connector) {
        this.connector = connector;
    }

    public void move (Direction direction, int id) {
        String output = "m " + id + " " + (direction == Direction.UP ? 1 : -1);
        connector.printLine(output);
    }

    public void stop (int id) {
        connector.printLine("m " + id + " 0");
    }

    public void openDoor(int id) {
        connector.printLine("d " + id + " 1");
    }

    public void closeDoor(int id) {
        connector.printLine("d " + id + " -1");
    }
}
