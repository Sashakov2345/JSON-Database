package server.commands;

import server.Database;

public abstract class Command {
    Database database;
    public Command(Database database) {
        this.database=database;
    }

    public abstract void execute();
}
