package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.base.Node;
import cz.cvut.fel.dsva.semestralka.service.ChatService;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;

@Slf4j
public class PrintStatusCommandHandler implements CommandHandler{
    private ChatService chatService;

    public PrintStatusCommandHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handle(String[] args, Node node) {
        if (chatService == null) {
            log.error("ChatService is not initialized. Cannot print status.");
            return;
        }
        try {
            chatService.printStatus(node);
        } catch (RemoteException e) {
            log.error("Remote communication error: {}", e.getMessage());
        }
    }
}
