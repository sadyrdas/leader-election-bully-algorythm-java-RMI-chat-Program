package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.rmi.RemoteException;

@Slf4j
@Getter
@Setter
public class GetAddressesCommandHandler implements CommandHandler {
    private ChatServiceImpl chatService;

    public GetAddressesCommandHandler(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handle(String[] args, Node node) {
        if (chatService == null) {
            log.error("ChatService is not initialized. Cannot send message.");
            return;
        }
        try {
            log.info("Hello, this is your neighbours");
            chatService.getAddressesOfNeighbours();
        } catch (RemoteException e) {
            log.error("Something went wrong: " + e.getMessage());
        }
    }
}
