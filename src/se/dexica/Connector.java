package se.dexica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by dexter on 04/03/15.
 */
public class Connector {
    private final Socket socket;
    private final int port = 4711;
    private BufferedReader in;
    private PrintWriter out;

    public Connector() throws IOException {
        socket = new Socket(InetAddress.getLocalHost(), port);
    }

    public void connect () throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public String readLine () throws IOException {
        return in.readLine();
    }

    public void printLine () {
        out.println("m 1 1");
    }
}
