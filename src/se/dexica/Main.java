package se.dexica;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Connector connector = new Connector();
        connector.connect();
        String line;
        connector.readLine();
        connector.printLine();
        while ((line = connector.readLine()) != null) {
            System.out.println(line);
        }
    }
}
