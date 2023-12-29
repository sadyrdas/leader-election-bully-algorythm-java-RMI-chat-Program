package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.service.ChatService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;

@Slf4j
@Getter
@Setter
public class GetLeaderStatusCommandHandler implements CommandHandler{
    private ChatService chatService;

    public GetLeaderStatusCommandHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handle(String[] args, Node node) {
        if (chatService == null) {
            log.error("ChatService is not initialized. Cannot send message.");
            return;
        }
        try{
            chatService.checkStatusOfLeader(node.getNodeId());
        }catch (RemoteException e) {
            log.error("Remote communication error: {}", e.getMessage());
        }catch (NumberFormatException e){
            log.error("Id of receiver must be number! Try again");
        }
    }
}
