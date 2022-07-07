package client;

import java.util.Objects;

public class Request {
    private String type;
    private String key;
    private String value;

    public Request(String type) {
        this(type,null,null);
    }

    public Request(String type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public Request(String type, String key) {
        this(type,key,null);
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
