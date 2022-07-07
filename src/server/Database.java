package server;

import client.Request;
import com.google.gson.*;
import server.commands.Command;
import server.commands.Controller;
import server.commands.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {

    private JsonElement key;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();

    public JsonElement getKey() {
        return key;
    }

    public JsonElement getValue() {
        return value;
    }

    private String type;
    private JsonElement value;
    private boolean isExit = false;
    private JsonObject response;
    final private JsonElement error = JsonParser.parseString("ERROR");
    final private JsonElement ok = JsonParser.parseString("OK");
    final private JsonElement NSK = JsonParser.parseString("\"No such key\"");
    private Controller controller = new Controller();
    private Command command;
    private Deque<String> keyQueue = new ArrayDeque<>();
//    final static File file = new File(System.getProperty("user.dir"), "JSON Database\\task\\src\\server\\data\\db.json");
        final static File file = new File(System.getProperty("user.dir"), "\\src\\server\\data\\db.json");
    private JsonObject base;

    private void getKeyQueue(JsonElement key) {
        JsonElement jsKey = key;
        if (jsKey.isJsonPrimitive()) {
            keyQueue.add(jsKey.getAsString());
            return;
        }
        if (jsKey.isJsonArray()) {
            JsonArray jsArrayKey = jsKey.getAsJsonArray();
            try {
                for (var k : jsArrayKey) {
                    keyQueue.add(k.getAsString());
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean searchAndDelete(JsonElement jsEl) {
        if (keyQueue.size() == 1) {
            if (jsEl.isJsonObject()) {
                JsonObject jsObj = jsEl.getAsJsonObject();
                String key = keyQueue.poll();
                JsonElement removed = jsObj.remove(key);
                if (!Objects.equals(removed, null)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        String key = keyQueue.poll();
        if (jsEl.isJsonPrimitive() && keyQueue.size() > 0) {
            return false;
        }
        if (jsEl.isJsonObject()) {
            JsonObject jsObj = jsEl.getAsJsonObject();
            JsonElement jsChild = jsObj.get(key);
            if (Objects.equals(jsChild, null)) {
                return false;
            }
            return searchAndDelete(jsChild);

        }
        return false;
    }

    private JsonObject createPathToValue(JsonElement value) {
        JsonObject jsonObject = new JsonObject();
        if (keyQueue.size() != 1) {
            String key = keyQueue.poll();
            jsonObject.add(key, createPathToValue(value));
            System.out.println(jsonObject);
            return jsonObject;
        }
        String key = keyQueue.poll();
        jsonObject.add(key, value);
        System.out.println(jsonObject);
        return jsonObject;
    }

    private boolean searchAndSet(JsonElement jsEl, JsonElement value) {
        if (keyQueue.size() == 1) {
            String key = keyQueue.poll();
            if (jsEl.isJsonObject()) {
                JsonObject jsObj = jsEl.getAsJsonObject();
                jsObj.add(key, value);
                return true;
            }
        }
        String key = keyQueue.poll();
        if (jsEl.isJsonObject()) {
            JsonObject jsObj = jsEl.getAsJsonObject();
            JsonElement jsChild = jsObj.get(key);
            if (!Objects.equals(jsChild, null)) {
                return searchAndSet(jsChild, value);
            } else {
                jsObj.add(key, createPathToValue(value));
                return true;
            }
        }
        return false;
    }


    private JsonElement search(JsonElement jsEl) {
        if (keyQueue.isEmpty()) {
            return jsEl;
        }
        String key = keyQueue.poll();
        if (jsEl.isJsonPrimitive()) {
            return null;
        }
        if (jsEl.isJsonObject()) {
            JsonObject jsObj = jsEl.getAsJsonObject();
            JsonElement jsChild = jsObj.get(key);
            if (Objects.equals(jsChild, null)) {
                return null;
            }
            return search(jsChild);

        }
        return null;
    }

    private void parseInput(String input) throws RuntimeException {
        JsonElement rEl = JsonParser.parseString(input);
        JsonObject request = rEl.getAsJsonObject();
        type = request.get("type").getAsString();
        response = new JsonObject();
        switch (type) {
            case "set":
                key = request.get("key");
                value = request.get("value");
                command = new setCommand(this);
                break;
            case "get":
                key = request.get("key");
                command = new getCommand(this);
                break;
            case "delete":
                key = request.get("key");
                command = new deleteCommand(this);
                break;
            case "exit":
                command = new exitCommand(this);
                break;
            default:
                throw new RuntimeException();
        }
    }


    public JsonObject accessDatabase(String input) {
        try {
            base = readFromFile();
            parseInput(input);
            controller.setCommand(command);
            controller.executeCommand();
        } catch (RuntimeException e) {
            response.add("response", error);
            response.add("reason", NSK);
        } finally {
            return response;
        }
    }

    public boolean getExit() {
        return isExit;
    }

    public void setExit(boolean exit) {
        isExit = exit;
        response.add("response", ok);
    }

    public boolean set(JsonElement key, JsonElement value) {
        boolean isSet = false;
        getKeyQueue(key);
        if (!keyQueue.isEmpty()) {
            isSet = searchAndSet(base, value);
            if (isSet) {
                response.add("response", ok);
                writeToFile();
                return true;
            }
        }
        response.add("response", error);
        response.add("reason", NSK);
        return false;
    }

    public void get(JsonElement key) {
        JsonElement value;
        getKeyQueue(key);
        if (!keyQueue.isEmpty()) {
            String firstKey = keyQueue.poll();
            JsonElement jsEl = base.get(firstKey);
            if (!Objects.equals(jsEl, null)) {
                value = search(jsEl);
                if (!Objects.equals(value, null)) {
                    response.add("response", ok);
                    response.add("value", value);
                    return;
                }
            }
        }
        response.add("response", error);
        response.add("reason", NSK);
    }

    public boolean delete(JsonElement key) {
        boolean isRemoved = false;
        getKeyQueue(key);
        if (!keyQueue.isEmpty()) {
            isRemoved = searchAndDelete(base);
            if (isRemoved) {
                response.add("response", ok);
                writeToFile();
                return true;
            }
        }
        response.add("response", error);
        response.add("reason", NSK);
        return false;
    }

    private JsonObject readFromFile() {
        readLock.lock();
        try (Reader reader = new FileReader(file)) {
            JsonElement baseEl = JsonParser.parseReader(reader);
            if (baseEl.isJsonNull()) {
                baseEl = JsonParser.parseString("{}");
            }
            if (baseEl.isJsonObject()) {
                JsonObject baseObj = baseEl.getAsJsonObject();
                return baseObj;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in writeToFile");
        } finally {
            readLock.unlock();
        }
        return null;
    }

    private void writeToFile() {
        writeLock.lock();
        try (FileWriter writer = new FileWriter(file, false)) {
            String json = new Gson().toJson(base);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in writeToFile");
        } finally {
            writeLock.unlock();
        }
    }


}
