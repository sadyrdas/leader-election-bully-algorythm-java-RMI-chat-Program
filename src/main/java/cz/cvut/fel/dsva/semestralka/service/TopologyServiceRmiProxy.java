package cz.cvut.fel.dsva.semestralka.service;

import com.sun.jdi.request.ClassUnloadRequest;
import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Address;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
public class TopologyServiceRmiProxy {
    private Node myNode;
    public TopologyServiceRmiProxy(Node node) {
        super();
        this.myNode = node;
    }

    public void notifyOtherNodes() {
        List<Address> otherNodes = myNode.getNeighbours().getNeighbours().stream()
                .filter(node -> !node.equals(myNode.getNeighbours().getLeaderNode()))  // Exclude the leader itself
                .collect(Collectors.toList());
        for (Address otherNode : otherNodes) {
            try {
                ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                otherNodeService.notifyAboutUpdatedNeighbors(otherNodes);
            } catch (RemoteException e) {
                log.error("Error notifying node " + otherNode + ": " + e.getMessage());
            }
        }
    }

    public void repairTopologyAfterJoin(Address newNeighbor) {
        List<Address> neighbors = new ArrayList<>(myNode.getNeighbours().getNeighbours());

        for (Address otherNode : neighbors) {
            try {
                ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                otherNodeService.repairTopologyAfterJoin(newNeighbor);
            } catch (RemoteException e) {
                log.error("Error notifying node " + otherNode + ": " + e.getMessage());
            }
        }
    }

    public void repairTopologyAfterLogOut(int nodeId) {
        List<Address> neighbors = new ArrayList<>(myNode.getNeighbours().getNeighbours());

        for (Address otherNode : neighbors) {
            try {
                ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                otherNodeService.repairTopologyAfterLogOut(nodeId);
            } catch (RemoteException e) {
                log.error("Error notifying node " + otherNode + ": " + e.getMessage());
            }
        }
    }

    public synchronized void repairTopologyWithNewLeader(Address address) {
        List<Address> neighbors = new ArrayList<>(myNode.getNeighbours().getNeighbours());
        for (Address otherNode : neighbors) {
            try {
                ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                otherNodeService.repairTopologyWithNewLeader(neighbors, address);
                otherNodeService.changeRmi(address);
                myNode.getCommunicationHUB().setRmiProxy(address);
            } catch (RemoteException e) {
                log.error("Error notifying node " + otherNode + ": " + e.getMessage());
            }
        }
    }

    public void broadcastLogout() {
        String logoutMessage = "Node " + myNode.getNodeId() + " has logged out.";
        // Create a copy of the neighbours list to avoid ConcurrentModificationException
        // Iterate over the copy and notify each neighbour
        Address leaderNode = myNode.getNeighbours().getLeaderNode();
        try {
            ChatService leader = myNode.getCommunicationHUB().getRMIProxy(leaderNode);
            leader.receiveMessage(logoutMessage, myNode.getNodeId());
        } catch (RemoteException e) {
            log.error("Failed to broadcast logout message to leader {}: {}", leaderNode, e.getMessage());
        }

        // Clear the original list after successful iteration
        myNode.getNeighbours().getNeighbours().clear();
    }

    public void showCurrentTopology(List<Address> otherNodes) {
        log.info("Received updating current topology from Leader with id {}", myNode.getNeighbours().getLeaderNode().getNodeID());
        System.out.println("Current topology:");
        System.out.printf("Your address: %s%n", myNode.getAddress().getHost());
        System.out.printf("Your neighbour: %s%n", otherNodes);
        System.out.printf("Leader: %s%n", myNode.getNeighbours().getLeaderNode());
    }

}
