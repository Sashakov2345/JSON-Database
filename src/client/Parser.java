package client;

import com.beust.jcommander.Parameter;


public class Parser {
    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getFileName() {
        return fileName;
    }

    @Parameter(names = "-t", description = "type of the request")
    private String type;

    @Parameter(names = "-k", description = "index of the cell")
    private String key;

    @Parameter(names = "-v", description = "value to save in the database")
    private String value;

    @Parameter(names = "-in", description = "read a request from that file. The file will be stored in /client/data")
    private String fileName;

}
