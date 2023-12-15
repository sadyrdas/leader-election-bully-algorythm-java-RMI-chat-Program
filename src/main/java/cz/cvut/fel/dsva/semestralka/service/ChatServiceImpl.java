package cz.cvut.fel.dsva.semestralka.service;

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

import java.util.List;

@Slf4j
@Getter
@Setter
public class ChatServiceImpl implements ChatService {
    private Node myNode;
    private DSNeighbours neighbours;
    private CommunicationHUB communicationHUB;
    private BullyAlgorithm bullyAlgorithm;

    public ChatServiceImpl(Node node) throws RemoteException {
        super();
        this.myNode = node;
        this.neighbours = new DSNeighbours(node.getAddress());
        this.communicationHUB = new CommunicationHUB(node);
        this.bullyAlgorithm = new BullyAlgorithm(node);
    }


    @Override
    public DSNeighbours join(Address addr) throws RemoteException {
        log.info("Join was called ...");
        DSNeighbours dsNeighbours = myNode.getNeighbours();
        if (addr.compareTo(myNode.getAddress()) == 0) {
            log.info("You are the first and leader!");
            return dsNeighbours;
        } else {
            log.info("Someone is joining");
            notifyJoin(addr);
            dsNeighbours.addNewNode(addr);
            log.info("Neighbours after JOIN: {}", myNode.getNeighbours());
            return dsNeighbours;
        }
    }

    @Override
    public void sendMessage(int receiverId, String message) throws RemoteException {
        try {
            Address destinationAddress = communicationHUB.getActiveNeighbours().getAddressById(receiverId);
            if (destinationAddress != null) {
                ChatService destinationNode = communicationHUB.getRMIProxy(destinationAddress);
                destinationNode.receiveMessage(message);
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

    }

    @Override
    public void printStatus(Node node) throws RemoteException {
        log.info("This node's id is" + " " + node.getNodeId() + " " + " with address" + " " + node.getAddress() + " " + "with Neighbors" + " " + node.getNeighbours().getNeighbours());
    }

    @Override
    public void receiveMessage(String message) throws RemoteException {
        log.info("Received message: {}", message);
    }

    @Override
    public void logIN(Node node) throws RemoteException {

    }

    @Override
    public void logOUT(Node node) throws RemoteException {

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
            Address destinationAddress = communicationHUB.getActiveNeighbours().getAddressById(receiverId);
            if (destinationAddress != null) {
                ChatService destinationNode = communicationHUB.getRMIProxy(destinationAddress);
                destinationNode.receiveMessage("Hello from Node " + myNode.getNodeId());;
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
                ChatService neighbourProxy = communicationHUB.getRMIProxy(neighbour);
                neighbourProxy.receiveMessage(message);
            } catch (RemoteException e) {
                log.error("Failed to broadcast message to neighbour {}: {}", neighbour, e.getMessage());
            }
        }
    }

    @Override
    public String getAddressesOfNeighbours() throws RemoteException {
        List<Address> neighbours = communicationHUB.getActiveNeighbours().getNeighbours();
        log.info("Your neighbours {}", neighbours);
        return null;
    }

    @Override
    public void notifyJoin(Address newNeighbor) throws RemoteException {
        if (neighbours.isLeaderPresent()){
            log.info("Received notification about a new join: {}", newNeighbor);
        }
    }
}
