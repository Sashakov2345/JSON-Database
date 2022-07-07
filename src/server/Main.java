package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    final static String address = "127.0.0.1";
    final static int port = 23456;
    static ExecutorService executor = Executors.newFixedThreadPool(4);
    private Socket socket;
    private static ServerSocket server;

    static private boolean Exit = false;

    public static boolean isExit() {
        return Exit;
    }

    public static void setExit(boolean exit) {
        Main.Exit = exit;
    }

    public static void main(String[] args) {
        System.out.println("Server started!");
        try {
            server = new ServerSocket(port, 50, InetAddress.getByName(address));
            while (!isExit()) {
                try {
                    Main main = new Main();
                    main.socket = server.accept();
                    Connection connection = new Connection(main.socket, main.server);
                    executor.submit(connection);
                } catch (IOException e){

                }
            }
        } catch (IOException e) {

        } finally {
            executor.shutdown();
        }
    }
}


