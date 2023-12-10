package cz.cvut.fel.dsva.semestralka.pattern.commandHandler;

import cz.cvut.fel.dsva.semestralka.base.Message;
import cz.cvut.fel.dsva.semestralka.base.Node;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.Arrays;

@Slf4j
@Getter
@Setter
public class SendMessageCommandHandler implements CommandHandler {
    private static final int maxArguments = 2;
    private ChatServiceImpl chatService;

    public SendMessageCommandHandler(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handle(String[] args, Node node)  {
        if (!isValidCommand(args)){
            return;
        }
        if (chatService == null) {
            log.error("ChatService is not initialized. Cannot send message.");
            return;
        }
        try{
            int receiverId = Integer.parseInt(args[0]);
            String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            int senderId = node.getNodeId();
            Message message = new Message(senderId, receiverId, msg);
            if(msg.isEmpty()){
                log.warn("Your message is empty. Please rewrite your message");
                return;
            }
            chatService.sendMessage(message.getReceiverID(), message.getMsg());
        }catch (RemoteException e) {
            log.error("Remote communication error: {}", e.getMessage());
        }catch (NumberFormatException e){
            log.error("Id of receiver must be number! Try again");
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
