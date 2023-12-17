package cz.cvut.fel.dsva.semestralka.bully;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.communication.CommunicationHUB;
import cz.cvut.fel.dsva.semestralka.service.ChatService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.Comparator;

@Slf4j
@Getter
@Setter
public class BullyAlgorithm{
    private Node node;
    private DSNeighbours dsNeighbours;
    public BullyAlgorithm(Node node) {
        this.node = node;
    }
    public Address findHighestPriorityNode() {
        dsNeighbours = node.getNeighbours(); // Update dsNeighbours
        Address address = dsNeighbours.getNeighbours().stream()
                .max(Comparator.comparingLong(Address::getNodeID))
                .orElse(null);
        dsNeighbours.setLeaderNode(address);
        log.info("New leader in our chat is: {}", address);
        return address;
    }

    public void notifyLeader(String event) {
        dsNeighbours = node.getNeighbours();
        Address leaderAddress = dsNeighbours.getLeaderNode();
        if (leaderAddress != null && leaderAddress.equals(node.getAddress())) {
            // This node is the leader, perform the notification logic here
            log.info("I am the leader, notifying: {}", event);
            try {
                ChatService leaderNode = node.getCommunicationHUB().getRMIProxy(leaderAddress);
                leaderNode.notifyLeaderAboutEvent(event);
            } catch (RemoteException e) {
                log.error("Failed to notify the leader: {}", e.getMessage());
            }
        } else {
            // The current node is not the leader; find and notify the new leader
            Address newLeaderAddress = dsNeighbours.getLeaderNode();
            if (newLeaderAddress != null) {
                log.info("Notifying the new leader: {}", event);
                try {
                    ChatService newLeaderNode = node.getCommunicationHUB().getRMIProxy(newLeaderAddress);
                    newLeaderNode.notifyLeaderAboutEvent(event);
                } catch (RemoteException e) {
                    log.error("Failed to notify the new leader: {}", e.getMessage());
                }
            } else {
                log.error("Couldn't find Leader or something wrong");
            }
        }
    }

}
