package server.commands;

import server.Database;

public class exitCommand extends Command {
    public exitCommand(Database database) {
        super(database);
    }

    @Override
    public void execute() {
        database.setExit(true);
    }
}
