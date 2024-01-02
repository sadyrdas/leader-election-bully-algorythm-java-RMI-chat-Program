package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;

@Getter
@Setter
@Slf4j
public class LogOutForceCommandHandler implements CommandHandler {
    private ChatServiceImpl chatService;

    public LogOutForceCommandHandler(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handle(String[] args, Node node) {
        if (chatService == null) {
            log.error("ChatService is not initialized. Cannot send message.");
            return;
        }
        try {
            chatService.logOUTForce();
        }catch (RemoteException e) {
            log.error("Something is wrong: " + e.getMessage());
        }
    }
}
