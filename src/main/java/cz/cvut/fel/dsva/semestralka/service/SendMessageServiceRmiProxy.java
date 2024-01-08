package cz.cvut.fel.dsva.semestralka.service;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Address;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

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
        Address destinationAddress = myNode.getNeighbours().getAddressById(receiverId);
        Runnable messageTask = () -> {
            try {
                if (destinationAddress != null && destinationAddress.isOnline() && myNode.getBullyAlgorithm().isLeaderAlive()) {
                    ChatService destinationNode = myNode.getCommunicationHUB().getRMIProxy(destinationAddress);
                    destinationNode.receiveMessage(message, senderId);
                    sendResponseToNodeFromLeader(senderId, receiverId, "OK");
                } else {
                    handleOfflineNode(senderId, receiverId, destinationAddress);
                }
            } catch (RemoteException e) {
                handleOfflineNode(senderId, receiverId, destinationAddress);
            }
        };

        long delay = 1; // Delay in seconds
        myNode.getScheduler().schedule(messageTask, delay, TimeUnit.SECONDS);
    }

    private void handleOfflineNode(long senderId, int receiverId, Address destinationAddress) {
        sendResponseToNodeFromLeader(senderId, receiverId, "FAIL, Node is offline");
        setSentMessageStatus(false);
        if (destinationAddress != null && myNode.getBullyAlgorithm().isLeaderAlive()) {
            Address sender = myNode.getNeighbours().getAddressById((int) senderId);
            Address leaderNode = myNode.getNeighbours().getLeaderNode();
            try {
                ChatService senderChatService = myNode.getCommunicationHUB().getRMIProxy(sender);
                senderChatService.logInfo("DestinationNode has logout force.");
            } catch (RemoteException e) {
                log.info("Error notifying node. Node is dead " + sender);
                log.info("Remove dead node");
                myNode.getNeighbours().removeNode(sender);
                sentMessageStatus = false;
            }
            try {
                ChatService receiverChatService = myNode.getCommunicationHUB().getRMIProxy(leaderNode);
                receiverChatService.logInfo("DestinationNode has logout force. Start repair topology");
            } catch (RemoteException e) {
                log.error("Error notifying sender " + e.getMessage());
                log.info("Error notifying node. Node is dead " + leaderNode);
                log.info("Remove dead node");
                myNode.getNeighbours().removeNode(leaderNode);
                sentMessageStatus = false;
            }
            myNode.getTopologyServiceRmiProxy().notifyAboutLogout(destinationAddress);
            myNode.getTopologyServiceRmiProxy().repairTopologyAfterLogOut(receiverId);
        }
    }



    public void sendResponseToNodeFromLeader(long senderId, long receiverId, String message){
        Address sender = myNode.getNeighbours().getAddressById((int) senderId);
        try {
            ChatService senderChatService = myNode.getCommunicationHUB().getRMIProxy(sender);
            senderChatService.notifyNodeAboutSentMessage( senderId,receiverId, message);
        } catch (RemoteException e) {
            log.info("Error notifying node. Node is dead " + sender);
            log.info("Remove dead node");
            myNode.getNeighbours().removeNode(sender);
            sentMessageStatus = false;
        }
    }
}
