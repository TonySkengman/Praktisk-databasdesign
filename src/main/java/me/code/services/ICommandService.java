package me.code.services;

import me.code.commands.Command;

public interface ICommandService {

    void registerCommand(Command command);
    void executeCommand(String commandInput);
}
