package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.base.Node;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;

@Slf4j
@Getter
@Setter
public class HelpCommandHandler implements CommandHandler{
    private ChatServiceImpl chatService;

    public HelpCommandHandler(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handle(String[] args, Node node) {
        if (chatService == null) {
            log.error("ChatService is not initialized. Cannot print commands.");
            return;
        }
        try {
            chatService.help();
        } catch (RemoteException e) {
            log.error("Remote communication error: {}", e.getMessage());
        }
    }
}
