package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.base.Node;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiveMessageCommandHandler implements CommandHandler{
    private ChatServiceImpl chatService;
    @Override
    public void handle(String[] args, Node node) {
        //TODO
    }
}
