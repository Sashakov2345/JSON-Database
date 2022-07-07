package server.commands;

import server.Database;

public class setCommand extends Command {
    public setCommand(Database database) {
        super(database);
    }

    @Override
    public void execute() {
        database.set(database.getKey(), database.getValue());
    }
}
