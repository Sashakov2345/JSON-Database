package server.commands;

import server.Database;

public class deleteCommand extends Command {
    public deleteCommand(Database database) {
        super(database);
    }

    @Override
    public void execute() {
        database.delete(database.getKey());
    }
}
