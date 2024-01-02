package cz.cvut.fel.dsva.semestralka.bully;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.service.ChatService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
public class BullyAlgorithm{
    private Node myNode;
    private DSNeighbours dsNeighbours;
    private boolean isLeaderAlive = true;
    public BullyAlgorithm(Node node) {
        this.myNode = node;
    }
    public Address getHighestPriorityNode(List<Address> highestPriorityNodes) {
        Address address = highestPriorityNodes.stream()
                .max(Comparator.comparingLong(Address::getNodeID))
                .orElse(null);
        return address;
    }

    public List<Address> findAllNodesWithHigherID(long senderId) {
        dsNeighbours = myNode.getNeighbours();
        Address leader = dsNeighbours.getLeaderNode();
        Address address = dsNeighbours.getAddressById((int) senderId);
        List<Address> otherNodes = dsNeighbours.getNeighbours().stream()
                .filter(node -> !node.equals(leader))
                .filter(node -> node.getNodeID() > senderId)
                .collect(Collectors.toList());
        if (otherNodes.isEmpty()){
            otherNodes.add(address);
        }
        log.info("Filtered nodes: {}", otherNodes);
        return otherNodes;

    }

    public void broadCastStatusCheckOfLeader(long senderId){
        Address address = myNode.getNeighbours().getAddressById((int) senderId);
        try {
            ChatService service = myNode.getCommunicationHUB().getRMIProxy(address);
            service.sendResponseAboutLeader();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendElectionProxy(long senderId) {
        List<Address> otherNodes = findAllNodesWithHigherID(senderId);
        Address address = myNode.getNeighbours().getAddressById((int) senderId);
        if (otherNodes.size() == 1 && otherNodes.contains(address)) {
            log.info("No other pretends to be a leader");
        } else {
            for (Address otherNode : otherNodes) {
                try {
                    ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                    otherNodeService.receiveMessage("Leader is out, i will start election", senderId);
                    otherNodeService.sendResponseForStartingElection(senderId, otherNode.getNodeID());
                } catch (RemoteException e) {
                    log.error("Error notifying node " + otherNode + ": " + e.getMessage());
                }
            }
        }
    }

    public void startElectionAgain(long senderId){
        List<Address> otherNodes = findAllNodesWithHigherID(senderId);
        for (Address otherNode : otherNodes) {
            try {
                ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                otherNodeService.startElectionAgain(otherNodes);
            } catch (RemoteException e) {
                log.error("Error notifying node " + otherNode + ": " + e.getMessage());
            }
        }
    }

    public void setFutureLeader(List<Address> highestPriorityNodes){
        Address leaderNode = myNode.getNeighbours().getLeaderNode();
        Address address = getHighestPriorityNode(highestPriorityNodes);
        List<Address> otherNodes = highestPriorityNodes.stream()
                .filter(node -> !node.equals(address))
                .collect(Collectors.toList());
        String message = "Relax, i will take care of this";
        for (Address otherNode : otherNodes){
            try {
                ChatService chatService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                chatService.receiveMessage(message, address.getNodeID());
            }catch (RemoteException e) {
                log.error("Couldn't get responses");
            }
        }
        try {
            ChatService futureLeaderService = myNode.getCommunicationHUB().getRMIProxy(address);
            futureLeaderService.logInfo("Now i will become leader and repair topology");
            futureLeaderService.election(address, leaderNode);
        }catch (RemoteException e) {
            log.error("Couldn't get responses");
        }
    }


    public void getResponseForStartingElection(long senderId, long receiverId){
        Address address = myNode.getNeighbours().getAddressById((int) senderId);
        try {
            ChatService chatService = myNode.getCommunicationHUB().getRMIProxy(address);
            chatService.receiveMessage("OK", receiverId);
        }catch (RemoteException e) {
            log.error("Couldn't get responses");
        }
    }

}
