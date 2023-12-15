package cz.cvut.fel.dsva.semestralka;

import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.base.Message;
import cz.cvut.fel.dsva.semestralka.bully.BullyAlgorithm;
import cz.cvut.fel.dsva.semestralka.communication.CommunicationHUB;
import cz.cvut.fel.dsva.semestralka.service.ChatService;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
public class Node implements  Runnable {
    private Long nodeId;
    private String myIP = "127.0.0.1";
    private int myPort = 2210;
    private Address address;
    private DSNeighbours neighbours;
    private Boolean isLeader = false;
    private List<Message> messageList;
    private ChatCLI chatCLI;
    public static Node thisNode = null;
    private CommunicationHUB communicationHUB;
    private Address otherNodeAddress;
    private String otherNodeIP = "127.0.0.1";
    private int otherNodePort= 2210;
    private ChatService chatService;
    private BullyAlgorithm bullyAlgorithm;
    public static String nameRMI = "ChatService";




    public Node(String[] args) {
        if (args.length == 3) {
            nodeId = (long) Integer.parseInt(args[0]);
            myIP  = otherNodeIP = args[1];
            myPort  = otherNodePort =  Integer.parseInt(args[2]);
        } else if(args.length == 5) {
            nodeId = (long) Integer.parseInt(args[0]);
            myIP = args[1];
            myPort = Integer.parseInt(args[2]);
            otherNodeIP = args[3];
            otherNodePort = Integer.parseInt(args[4]);
        }else {
            log.error("Invalid arguments to input, try again!");
        }
    }
    public void addMsgToList(Message message){
        messageList.add(message);
    }



    private ChatService startChatService(){
        System.setProperty("java.rmi.server.hostname", address.host);

        ChatService chatService = null;
        try {
            chatService = new ChatServiceImpl(this);

            ChatService skeleton = (ChatService) UnicastRemoteObject.exportObject(chatService, 40000 + address.port);

            Registry registry = LocateRegistry.createRegistry(address.port);
            registry.rebind(nameRMI, skeleton);
        } catch (RemoteException e) {
            log.error("Chat service - something wrong happened: " + e.getMessage());
        }
        log.info("Chat Service started!");
        return chatService;
    }
    @Override
    public void run() {
        address = new Address(myIP, myPort, nodeId);
        communicationHUB = new CommunicationHUB(this);

        try {
            if (nodeId == 1) {
                neighbours = new DSNeighbours(address);
                communicationHUB.setActiveNeighbours(neighbours);
                neighbours.addNewNode(address);
            } else {
                ChatService tmpNode = communicationHUB.getRMIProxy(new Address(otherNodeIP, otherNodePort));
                neighbours = tmpNode.join(address);
                communicationHUB.setActiveNeighbours(neighbours);
            }
        } catch (RemoteException e) {
            log.error("Error setting up neighbors: " + e.getMessage());
        }

        chatService = startChatService();

        try {
            chatCLI = new ChatCLI(this);
        } catch (RemoteException e) {
            log.error("Something wrong with ChatCLI " + e.getMessage());
        }

        new Thread(chatCLI).start();
    }




    public static void main(String[] args) {
        thisNode = new Node(args);
        thisNode.run();
    }
}
