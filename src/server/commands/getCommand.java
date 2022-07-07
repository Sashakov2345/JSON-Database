package server.commands;

import server.Database;

public class getCommand extends Command {
    public getCommand(Database database) {
        super(database);
    }

    @Override
    public void execute() {
        database.get(database.getKey());
    }
}
