package client;

import com.beust.jcommander.JCommander;
import com.google.gson.JsonParser;
import com.google.gson.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

public class Main {
    private String key;
    private String type;
    private String value = "";
    private String requestJson;
    private String fileName;
//    final static File folder = new File(System.getProperty("user.dir"),"JSON Database\\task\\src\\client\\data\\");
    final static File folder = new File(System.getProperty("user.dir"),"\\src\\client\\data\\");
    private Request request;
    private Parser parser;

    private void getJsonFromFile() {
        File file=new File(folder,fileName);
        try(Reader reader = new FileReader(file)){
            JsonElement r = JsonParser.parseReader(reader);
            requestJson=r.toString();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            System.out.println("Sent: " + requestJson);
        }
    }

    private void parseInput(String[] input) throws RuntimeException {
        parser = new Parser();
        JCommander.newBuilder()
                .addObject(parser)
                .build()
                .parse(input);

        fileName = parser.getFileName();
        if (!Objects.equals(fileName, null)) {
            getJsonFromFile();
            return;
        }
            key = parser.getKey();
            type = parser.getType();
        switch (type) {
            case "set":
                value = parser.getValue();
                request = new Request(type, key, value);
                break;
            case "exit":
                request = new Request(type);
                break;
            default:
                request = new Request(type, key);
                break;
        }
        requestJson = new Gson().toJson(request);
        System.out.println("Sent: " + requestJson);
    }


    final static String address = "127.0.0.1";
    final static int port = 23456;

    public static void main(String[] args) {
        Main main = new Main();
        try (Socket socket = new Socket(InetAddress.getByName(address), port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());) {
            System.out.println("Client started!");
            main.parseInput(args);
            output.writeUTF(main.requestJson);
            String dataIn = input.readUTF();
            System.out.println("Received: " + dataIn);
        } catch (IOException | RuntimeException e) {
            System.out.println(e.getMessage());
        }

    }
}
