package cz.cvut.fel.dsva.semestralka;

import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.base.Message;
import cz.cvut.fel.dsva.semestralka.bully.BullyAlgorithm;
import cz.cvut.fel.dsva.semestralka.communication.CommunicationHUB;
import cz.cvut.fel.dsva.semestralka.service.ChatService;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import cz.cvut.fel.dsva.semestralka.service.SendMessageServiceRmiProxy;
import cz.cvut.fel.dsva.semestralka.service.TopologyServiceRmiProxy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Getter
@Setter
public class Node implements  Runnable {
    private long nodeId;
    private String myIP ;
    private int myPort;
    private Address address;
    private DSNeighbours neighbours;
    private Boolean isLeader;
    private List<Message> messageList;
    private ChatCLI chatCLI;
    private TopologyServiceRmiProxy topologyServiceRmiProxy;
    private SendMessageServiceRmiProxy sendMessageServiceRmiProxy;
    public static Node thisNode = null;
    private CommunicationHUB communicationHUB;
    private String leaderNodeIP = "127.0.0.1";
    private int leaderPort = 50005;
    private long leaderId = 5;
    private ChatService chatService;
    private BullyAlgorithm bullyAlgorithm;
    public static String nameRMI = "ChatService";
    private final Object lock = new Object();
    private volatile Address targetNetworkAddress;





    public Node(String[] args) {
        if (args.length == 2) {
            nodeId =  Integer.parseInt(args[0]);
            myIP   = args[1];
            myPort   =  (int) (nodeId + 50000);
        } else {
            log.error("Invalid arguments to input, try again!");
        }
        
    }


    public int generatePort() {
        return (int) (nodeId + 50000);
    }


    public ChatService startChatService(){
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

    private boolean isTargetNetworkAddressSet = false;



    private boolean leaderSetTargetNetwork = false;



    private final CountDownLatch targetNetworkLatch = new CountDownLatch(1);

    // Other class members...

    public synchronized void setTargetNetworkAddress(String newLeaderNodeIP, int newLeaderPort) {
        targetNetworkAddress = new Address(newLeaderNodeIP, newLeaderPort);
        log.info("Target network address set: {}", targetNetworkAddress);

        targetNetworkLatch.countDown();
        notifyAll();// Signal that targetNetworkAddress is set
    }



    String bootstrapNodeIP = "127.0.0.1"; // Change this to the IP of your bootstrap node.
    int bootstrapNodePort = 50003;
    private synchronized void initializeCommunicationHUB() {
        synchronized (lock) {
            try {
                log.info("Attempting to connect to targetNetworkAddress: {}", targetNetworkAddress);
                ChatService leader = communicationHUB.getRMIProxy(targetNetworkAddress);
                // Existing node, continue with the current targetNetworkAddress
                neighbours = leader.join(address);
                setNeighbours(neighbours);


            } catch (RemoteException e) {
                log.error("Error joining existing network: " + e.getMessage());
                chatCLI.setReading(false);
            }
        }
    }

    @Override
    public synchronized void run() {
        address = new Address(myIP, myPort, nodeId);
        try {
            chatCLI = new ChatCLI(this);
        } catch (RemoteException e) {
            log.error("Something wrong with ChatCLI " + e.getMessage());
        }

        if (!chatCLI.isReading()) {
            log.info("Closing ChatCLI.");
            log.info("ChatService stopped!");
            chatCLI.stop();
        } else {
            startChatCLI();
        }

        chatService = startChatService();
        neighbours = new DSNeighbours(address);
        communicationHUB = new CommunicationHUB(this);
        topologyServiceRmiProxy = new TopologyServiceRmiProxy(this);
        sendMessageServiceRmiProxy = new SendMessageServiceRmiProxy(this);
        bullyAlgorithm = new BullyAlgorithm(this);

        // Check if it's the first node in the topology
        if (isFirstNode()) {
            // Connect to itself
            log.info("First node in the topology, connecting to itself.");
            setTargetNetworkAddress(leaderNodeIP, leaderPort);
        } else {
            try {
                targetNetworkLatch.await(); // Wait until targetNetworkAddress is set
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (isTargetNetworkAddressSet &&
                    !(targetNetworkAddress.host.equals(bootstrapNodeIP) && targetNetworkAddress.port == bootstrapNodePort)) {

                setTargetNetworkAddress(leaderNodeIP, leaderPort);
                log.info("Joining network using existing leader {}: {}", targetNetworkAddress.host, targetNetworkAddress.port);
            } else {

                joinNetworkUsingBootstrap();
            }

        }

        initializeCommunicationHUB();
    }

    private boolean isFirstNode() {
        // Check if nodeId is equal to 1
        return nodeId == 5;
    }
    private synchronized void joinNetworkUsingBootstrap() {
        // ...

        try {
            ChatService bootstrapNode = communicationHUB.getRMIProxy(new Address(bootstrapNodeIP, bootstrapNodePort));
            Address newLeader = bootstrapNode.getCurrentLeader();

            // Check if the leader is not alive before using bootstrap
            if (newLeader != null) {
                setTargetNetworkAddress(newLeader.host, newLeader.port);
                log.info("Joining network using leader {}: {}", newLeader.host, newLeader.port);
            } else {
                log.info("Leader is alive, continuing with current targetNetworkAddress.");
            }

        } catch (RemoteException e) {
            log.error("Error joining network using bootstrap: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        thisNode = new Node(args);
        thisNode.run();
    }
    private void startChatCLI(){
        chatCLI.printWelcomeMessage();
        new Thread(chatCLI).start();
    }
}
