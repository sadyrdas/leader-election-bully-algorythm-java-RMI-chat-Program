package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;

@Slf4j
@Getter
@Setter
public class SendHelloMessageCommandHandler implements CommandHandler {
    private static final int maxArguments = 1;
    private ChatServiceImpl chatService;

    public SendHelloMessageCommandHandler(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handle(String[] args, Node node) {
        if (!isValidCommand(args)){
            return;
        }
        if (chatService == null) {
            log.error("ChatService is not initialized. Cannot send message.");
            return;
        }
        try {
            int receiverId = Integer.parseInt(args[0]);
            chatService.sendHello(receiverId);
        }catch (RemoteException e) {
            log.error("Something is wrong: " + e.getMessage());
        }
    }

    private boolean isValidCommand(String[] args){
        if (args.length > maxArguments){
            log.error("Unexpected count of arguments. Expected {}, but got {}:{}",
                    maxArguments, args.length, Arrays.toString(args));
        }
        return true;
    }
}
