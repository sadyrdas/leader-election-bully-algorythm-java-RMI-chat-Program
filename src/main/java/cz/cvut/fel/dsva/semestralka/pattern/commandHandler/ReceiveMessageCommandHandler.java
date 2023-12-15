package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.rmi.RemoteException;

@Slf4j
public class ReceiveMessageCommandHandler implements CommandHandler {
    private ChatServiceImpl chatService;

    public ReceiveMessageCommandHandler(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handle(String[] args, Node node) {
        if (chatService == null) {
            log.error("ChatService is not initialized. Cannot send message.");
            return;
        }
        try {
            String msg = args[2];
            chatService.receiveMessage(msg);
        } catch (RemoteException e) {
            log.error("You don't have any messages");
        }
    }
}
