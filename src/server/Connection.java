package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection implements Runnable {
    final static String address = "127.0.0.1";
    final static int port = 23456;
    private Socket socket;
    private ServerSocket server;

    public Connection(Socket socket, ServerSocket server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        Database database = new Database();
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            String dataIn = input.readUTF();
            JsonObject response = database.accessDatabase(dataIn);
            String responseJson = new Gson().toJson(response);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeUTF(responseJson);
            socket.close();
            if(database.getExit()){
                Main.setExit(true);
                server.close();
            }
        } catch (IOException e) {
        }
    }
}
