package cz.cvut.fel.dsva.semestralka.service;

import cz.cvut.fel.dsva.semestralka.ChatCLI;
import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.base.Message;
import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.bully.BullyAlgorithm;
import cz.cvut.fel.dsva.semestralka.communication.CommunicationHUB;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
public class ChatServiceImpl implements ChatService {
    private Node myNode;

    public ChatServiceImpl(Node node) throws RemoteException {
        super();
        this.myNode = node;
    }


    @Override
    public DSNeighbours join(Address addr) throws RemoteException {
        log.info("Join was called ...");

        DSNeighbours dsNeighbours = myNode.getNeighbours();

        if (addr.compareTo(myNode.getAddress()) == 0) {
            // If the joining node is the first and leader
            log.info("You are the first and leader!");
            dsNeighbours.addNewNode(addr);
            return dsNeighbours;
        } else {
            log.info("Someone is join");
            updateNeighborsList(addr, true);
            myNode.getCommunicationHUB().setActiveNeighbours(dsNeighbours);

            notifyOtherNodes(dsNeighbours);
            log.info("Neighbours after JOIN: {}", dsNeighbours);
            return dsNeighbours;
        }
    }






    @Override
    public void updateNeighborsList(Address newNeighbor, boolean join) throws RemoteException {
        DSNeighbours dsNeighbours = myNode.getNeighbours();

        if (join) {
            // Add the new node to the neighbors
            dsNeighbours.addNewNode(newNeighbor);
            myNode.getCommunicationHUB().setActiveNeighbours(dsNeighbours);
            dsNeighbours.setLeaderNode(myNode.getCommunicationHUB().getActiveNeighbours().getLeaderNode());
            myNode.getCommunicationHUB().setActiveNeighbours(dsNeighbours);
            log.info("Node {} joined. Updated neighbors: {}", newNeighbor, dsNeighbours);
        } else {
            // Remove the departing node from the neighbors
            dsNeighbours.getNeighbours().remove(newNeighbor);

            log.info("Node {} left. Updated neighbors: {}", newNeighbor, dsNeighbours);
        }
    }


    private void notifyOtherNodes(DSNeighbours updatedNeighbors) {
        List<Address> otherNodes = myNode.getNeighbours().getNeighbours().stream()
                .filter(node -> !node.equals(myNode.getAddress()))  // Exclude the leader itself
                .collect(Collectors.toList());

        for (Address otherNode : otherNodes) {
            try {
                ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                otherNodeService.updateNeighbors(updatedNeighbors);
            } catch (RemoteException e) {
                log.error("Error notifying node " + otherNode + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void updateNeighbors(DSNeighbours updatedNeighbors) throws RemoteException {
        log.info("Received updated neighbors: {}", updatedNeighbors);
        myNode.getCommunicationHUB().setActiveNeighbours(updatedNeighbors);
    }







    @Override
    public void sendMessage(int receiverId, String message) throws RemoteException {
        try {
            Address destinationAddress = myNode.getCommunicationHUB().getActiveNeighbours().getAddressById(receiverId);
            if (destinationAddress != null) {
                ChatService destinationNode = myNode.getCommunicationHUB().getRMIProxy(destinationAddress);
                destinationNode.receiveMessage("Message from node with ID: "+ " " + myNode.getNodeId() + " " + "contains: " + " " + message);
            } else {
                log.error("Destination node with ID {} not found.", receiverId);
            }
        } catch (RemoteException e) {
            log.error("Failed to send message: " + e.getMessage());
        }
    }

    @Override
    public void help() throws RemoteException {
        log.info("'help' - Available commands ");
        log.info("'send' - Send message to the next node");
        log.info("send <id of target node> <Message>");
        log.info("'status' - Print your status");
        log.info("'mymessages' - Show list of received message");
        log.info("'hello' - Send hello message to the node with id");
        log.info("hello <id of target Node>");
        log.info("'neighbours' - Show list of neighbours");
        log.info("'logout' - Log out from chat" );

    }

    @Override
    public void printStatus(Node node) throws RemoteException {
        log.info("This node's id is" + " " + node.getNodeId() + " " + " with address" + " " + node.getAddress() + " " + "\nwith Neighbors" + " " + node.getNeighbours());
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        if (message.startsWith("Node") && message.contains("has logged out.")) {
            handleLogoutMessage(message);
        } else {
            log.info("Received message: {}", message);
        }
    }

    @Override
    public void notifyLeaderAboutEvent(String event) throws RemoteException {
        // Log the event or perform other actions based on different notifications
        log.info("Received notification from a node: {}", event);
    }


    private void handleLogoutMessage(String message) {
        // Extract node ID from the logout message and update the neighbors accordingly
        // Example: "Node 123 has logged out."
        String[] parts = message.split(" ");
        if (parts.length >= 2) {
            int nodeId = Integer.parseInt(parts[1]);
            DSNeighbours dsNeighbours = myNode.getNeighbours();
            dsNeighbours.removeNodeById(nodeId);
            myNode.getCommunicationHUB().setActiveNeighbours(dsNeighbours);
            log.info("Node {} has logged out. Updated neighbors: {}", nodeId, dsNeighbours.getNeighbours());
        }
    }


    @Override
    public void logOUT() throws RemoteException {
        DSNeighbours dsNeighbours = myNode.getNeighbours();
        dsNeighbours.getNeighbours().remove(myNode.getAddress());
        log.info("Started LogOut");

        broadcastLogout();
        myNode.getCommunicationHUB().setActiveNeighbours(dsNeighbours);
    }



    public void broadcastLogout() {
        String logoutMessage = "Node " + myNode.getNodeId() + " has logged out.";

        // Create a copy of the neighbours list to avoid ConcurrentModificationException
        List<Address> neighboursCopy = new ArrayList<>(myNode.getNeighbours().getNeighbours());

        // Iterate over the copy and notify each neighbour
        for (Address neighbour : neighboursCopy) {
            try {
                ChatService neighbourProxy = myNode.getCommunicationHUB().getRMIProxy(neighbour);
                neighbourProxy.receiveMessage(logoutMessage);
            } catch (RemoteException e) {
                log.error("Failed to broadcast logout message to neighbour {}: {}", neighbour, e.getMessage());
            }
        }

        // Clear the original list after successful iteration
        myNode.getNeighbours().getNeighbours().clear();
    }



    @Override
    public List<Message> getMessages(Node node) throws RemoteException {
        return null;
    }

    @Override
    public boolean hasNewMessages(Node node) throws RemoteException {
        return false;
    }

    @Override
    public void registerNode(Node node) throws RemoteException {

    }

    @Override
    public void sendHello(int receiverId) throws RemoteException {
        try {
            Address destinationAddress = myNode.getCommunicationHUB().getActiveNeighbours().getAddressById(receiverId);
            if (destinationAddress != null) {
                ChatService destinationNode = myNode.getCommunicationHUB().getRMIProxy(destinationAddress);
                destinationNode.receiveMessage("Hello from Node " + myNode.getNodeId());
            } else {
                log.error("Destination node with ID {} not found.", receiverId);
            }
        } catch (RemoteException e) {
            log.error("Failed to send message: " + e.getMessage());
        }
    }

    @Override
    public void broadcastMessage(String message) throws RemoteException {
        for (Address neighbour : myNode.getNeighbours().getNeighbours()) {
            try {
                ChatService neighbourProxy = myNode.getCommunicationHUB().getRMIProxy(neighbour);
                neighbourProxy.receiveMessage(message);
            } catch (RemoteException e) {
                log.error("Failed to broadcast message to neighbour {}: {}", neighbour, e.getMessage());
            }
        }
    }

    @Override
    public String getAddressesOfNeighbours() throws RemoteException {
        DSNeighbours activeNeighbours = myNode.getCommunicationHUB().getActiveNeighbours();


        if (!activeNeighbours.getNeighbours().isEmpty()) {
            log.info("Your neighbours {} \nand leader {}", activeNeighbours.getNeighbours(), activeNeighbours.getLeaderNode());
            return "Your neighbours: " + activeNeighbours.getNeighbours();
        } else {
            log.warn("No active neighbours found.");
            return "No active neighbours found.";
        }
    }


}
