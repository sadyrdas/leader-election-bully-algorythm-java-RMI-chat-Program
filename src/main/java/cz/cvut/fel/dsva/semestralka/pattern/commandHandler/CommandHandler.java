package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.Node;

public interface CommandHandler {
    void handle(String[] args, Node node);
}
