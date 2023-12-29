package cz.cvut.fel.dsva.semestralka.service;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Address;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;

@Slf4j
@Getter
@Setter
public class SendMessageServiceRmiProxy {
    private Node myNode;
    private boolean sentMessageStatus = true;
    public SendMessageServiceRmiProxy(Node node) {
        super();
        this.myNode = node;
    }

    public void notifyLeaderAboutSendMessage(long senderId, long receiverId, String message){
        Address leaderAddress = myNode.getNeighbours().getLeaderNode();
        try {
            ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(leaderAddress);
            otherNodeService.notifyLeaderAboutSendMessage(senderId, receiverId, message);
        } catch (RemoteException e) {
            sentMessageStatus = false;
            myNode.getBullyAlgorithm().setLeaderAlive(false);
        }
    }

    public void broadCastMessage(long senderId, int receiverId, String message){
        Address destinationAddress = myNode.getNeighbours().getAddressById( receiverId);
        try {
            if (destinationAddress != null && myNode.getBullyAlgorithm().isLeaderAlive()) {
                ChatService destinationNode = myNode.getCommunicationHUB().getRMIProxy(destinationAddress);
                destinationNode.receiveMessage(message, senderId);
                sendResponseToNodeFromLeader(senderId, receiverId, "OK");
            } else if(destinationAddress == null){
                sendResponseToNodeFromLeader(senderId, receiverId,"FAIL, Node is offline");
            }else {
                log.error("Leader is not alive receiver  will get info about this .");
                ChatService destinationNode = myNode.getCommunicationHUB().getRMIProxy(destinationAddress);
                destinationNode.receiveMessage("Someone was trying to send you message but \nLeader is not alive. Sorry, we will start election as fast as possible", receiverId);
                sentMessageStatus = false;
            }
        } catch (RemoteException e) {
            log.error("Failed to send message: Leader is out");
            sentMessageStatus = false;
        }
    }


    public void sendResponseToNodeFromLeader(long senderId, long receiverId, String message){
        Address sender = myNode.getNeighbours().getAddressById((int) senderId);
        try {
            ChatService senderChatService = myNode.getCommunicationHUB().getRMIProxy(sender);
            senderChatService.notifyNodeAboutSentMessage( senderId,receiverId, message);
        } catch (RemoteException e) {
            log.error("Error notifying sender " + e.getMessage());
            sentMessageStatus = false;
        }
    }
}
