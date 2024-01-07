package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Message;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Getter
@Setter
public class SendElectionMsgCommandHandler implements CommandHandler{
    private ChatServiceImpl chatService;

    public SendElectionMsgCommandHandler(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handle(String[] args, Node node) {
        if (chatService == null) {
            log.error("ChatService is not initialized. Cannot send message.");
            return;
        }
        try{
            chatService.electionByOldLeader(node.getAddress());
        }catch (RemoteException e) {
            log.error("Remote communication error: {}", e.getMessage());
        }
    }
}
