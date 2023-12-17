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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

@Slf4j
@Getter
@Setter
public class Node implements  Runnable {
    private Long nodeId;
    private Long otherNodeId;
    private String myIP ;
    private int myPort;
    private Address address;
    private DSNeighbours neighbours;
    private Boolean isLeader = false;
    private List<Message> messageList;
    private ChatCLI chatCLI;
    public static Node thisNode = null;
    private CommunicationHUB communicationHUB;
    private Address otherNodeAddress;
    private String leaderNodeIP = "127.0.0.1";
    private int leaderPort = 50001;
    private ChatService chatService;
    private BullyAlgorithm bullyAlgorithm;
    public static String nameRMI = "ChatService";
    private final Object objLock = new Object();




    public Node(String[] args) {
        if (args.length == 2) {
            nodeId = (long) Integer.parseInt(args[0]);
            myIP  =  args[1];
            myPort  = (int) (nodeId + 50000);
        } else {
            log.error("Invalid arguments to input, try again!");
        }
        
    }
    public void addMsgToList(Message message){
        messageList.add(message);
    }


    public long generateID() {
        String[] hostInParts = getMyIP().split("\\.");
        long sumOfIntegersinHost = 0L;
        long newID = 0L;
        for (int i = 0; i < hostInParts.length; i++) {
            int integerValue = Integer.parseInt(hostInParts[i]);
            sumOfIntegersinHost += integerValue;
            newID = sumOfIntegersinHost * getMyPort();
        }
        return newID;
    }

    public int generatePort() {
        return (int) (nodeId + 50000);
    }


    private ChatService startChatService(){
        System.setProperty("java.rmi.server.hostname", leaderNodeIP);

        ChatService chatService = null;
        try {
            chatService = new ChatServiceImpl(this);
            int registryPort = generatePort();

            ChatService skeleton = (ChatService) UnicastRemoteObject.exportObject(chatService, registryPort);

            try {
                Registry registry = LocateRegistry.createRegistry(registryPort);
                registry.rebind(nameRMI, skeleton);
            } catch (RemoteException e) {
                log.error("Failed to create registry on port: " + registryPort, e);
            }
        } catch (RemoteException e) {
            log.error("Chat service - something wrong happened: " + e.getMessage());
        }
        log.info("Chat Service started!");
        return chatService;
    }







    private final Object lock = new Object();

    private void initializeCommunicationHUB() {
        synchronized (lock) {
            Address targetNetworkAddress = new Address(leaderNodeIP, leaderPort);
            try {
                ChatService leader = communicationHUB.getRMIProxy(targetNetworkAddress);
                neighbours = leader.join(address);
                communicationHUB.setActiveNeighbours(neighbours);
                log.info("Joining network using leader {}: {}", targetNetworkAddress.host, targetNetworkAddress.port);
            } catch (RemoteException e) {
                log.error("Error joining existing network: " + e.getMessage());
            }
        }
    }





    @Override
    public void run() {
        address = new Address(myIP, myPort, nodeId);
        chatService = startChatService();
        neighbours = new DSNeighbours(address);
        communicationHUB = new CommunicationHUB(this);
        try {
            chatCLI = new ChatCLI(this);
        } catch (RemoteException e) {
            log.error("Something wrong with ChatCLI " + e.getMessage());
        }
        initializeCommunicationHUB();
        startChatCLI();
    }

    private void startChatCLI(){
        new Thread(chatCLI).start();
    }




    public static void main(String[] args) {
        thisNode = new Node(args);
        thisNode.run();

    }

    public boolean isFirstNode() {
        return true;
    }
}
