package me.code.commands;

import me.code.services.ITransactionService;

public abstract class Command {

    protected final String name;
    protected final String description;
    protected final ITransactionService transactionService;

    public Command(String name, String description, ITransactionService transactionService) {
        this.name = name;
        this.description = description;
        this.transactionService = transactionService;
    }

    public abstract void execute();

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " - " + description;
    }
}
