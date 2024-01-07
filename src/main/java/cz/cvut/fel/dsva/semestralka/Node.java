package cz.cvut.fel.dsva.semestralka;
import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.base.LogicalTimestamp;
import cz.cvut.fel.dsva.semestralka.bully.BullyAlgorithm;
import cz.cvut.fel.dsva.semestralka.communication.CommunicationHUB;
import cz.cvut.fel.dsva.semestralka.service.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.MDC;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@Slf4j
@Getter
@Setter
public class Node implements  Runnable {
    private final ScheduledExecutorService scheduler;
    private long nodeId;
    private String myIP ;
    private int myPort;
    private Address address;
    private DSNeighbours neighbours;
    private ChatCLI chatCLI;
    private TopologyServiceRmiProxy topologyServiceRmiProxy;
    private SendMessageServiceRmiProxy sendMessageServiceRmiProxy;
    public static Node thisNode = null;
    private CommunicationHUB communicationHUB;
    private String leaderNodeIP = "192.168.56.115";
    private int leaderPort = 50005;
    private long leaderId = 5;
    private ChatService chatService;
    private BullyAlgorithm bullyAlgorithm;
    public static String nameRMI = "ChatService";
    private final Object lock = new Object();
    private volatile boolean firstAttempt = true;
    private Address targetNetworkAddress = new Address(leaderNodeIP, leaderPort, leaderId);
    public synchronized boolean isFirstAttempt() {
        return firstAttempt;
    }

    public synchronized void setFirstAttempt(boolean firstAttempt) {
        this.firstAttempt = firstAttempt;
    }



    public Node(ScheduledExecutorService scheduler, String[] args) {
        this.scheduler = scheduler;
        if (args.length == 4) {
            nodeId =  Integer.parseInt(args[0]);
            myIP   = args[1];
            myPort   =  (int) (nodeId + 50000);
            leaderNodeIP = args[3];
            leaderId = Integer.parseInt(args[2]);
            leaderPort = (int) (leaderId + 50000);
        } else if (args.length ==2){
            nodeId =  Integer.parseInt(args[0]);
            myIP   = args[1];
            myPort   =  (int) (nodeId + 50000);
        }else {
            log.error("Invalid arguments to input, try again!");
        }
        
    }


    public int generatePort() {
        return (int) (nodeId + 50000);
    }


    public ChatService startChatService() throws RemoteException {
        ChatService chatService = null;
        System.setProperty("java.rmi.server.hostname", myIP);

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

    private synchronized void initializeCommunicationHUB() throws IOException, ParseException {
        synchronized (lock) {
            if (this.address.equals(targetNetworkAddress)) {
                firstAttempt = readStateFromFile(); // Read the state for the specific node
            }
            try {
                if (this.isFirstAttempt()) {
                    log.info("First attempt to connect to the network.");
                    log.info("Attempting to connect to targetNetworkAddress: {}", targetNetworkAddress);
                    ChatService leader = communicationHUB.getRMIProxy(targetNetworkAddress);
                    log.info("Joining network using leader {}: {}", targetNetworkAddress.host, targetNetworkAddress.port);
                    neighbours = leader.join(address);
                    setNeighbours(neighbours);

                } else if (!this.isFirstAttempt()){
                    // For subsequent attempts, ensure not connecting to self
                    if (this.address.equals(targetNetworkAddress)) {
                        log.info("Not first attempt. Old leader is dead, new leader is elected. Trying to join network using bootstrap.");
                        joinNetworkUsingBootstrap();
                    }
                }
            } catch (RemoteException e) {
                log.info("Error joining existing network: OldLeader is dead. Start connect to newLeader");
                joinNetworkUsingBootstrap();
            }
        }
    }
    public synchronized List<Address> getListOfAvailableNodes() {
        List<Address> availableNodes = new ArrayList<>();
        Address nodeAddress5 = new Address("192.168.56.115", 50005);
        Address nodeAddress4 = new Address("192.168.56.104", 50004);
        Address nodeAddress3 = new Address("192.168.56.103", 50003);
        Address nodeAddress2 = new Address("192.168.56.102", 50002);
        Address nodeAddress1 = new Address("192.168.56.101", 50001);
        availableNodes.add(nodeAddress5);
        availableNodes.add(nodeAddress4);
        availableNodes.add(nodeAddress3);
        availableNodes.add(nodeAddress2);
        availableNodes.add(nodeAddress1);
        return availableNodes;
    }

    private synchronized void joinNetworkUsingBootstrap() throws IOException, ParseException {
        boolean joined = false;
        // Retrieve and filter available nodes
        List<Address> availableNodes = getListOfAvailableNodes().stream()
                .filter(node -> node.port != myPort)
                .sorted((a, b) -> b.port - a.port)
                .collect(Collectors.toList());

        // Iterate over each node to try joining the network
        for (Address nodeAddress : availableNodes) {
            try {
                // Get RMI proxy for the node
                ChatService bootstrapNode = communicationHUB.getRMIProxy(nodeAddress);

                // Request the current leader from the node
                Address newLeader = bootstrapNode.getCurrentLeader();

                // If a leader is found, join the network
                if (newLeader != null) {
                    log.info("Joining network using newLeader {}: {}", newLeader.host, newLeader.port);
                    neighbours = bootstrapNode.join(address);
                    setNeighbours(neighbours);
                    joined = true;
                    break; // Break the loop as the network is successfully joined
                }
            } catch (Exception e) {
                log.warn("Failed to connect to node at {}:{}. Trying next node.", nodeAddress.host, nodeAddress.port);
            }
        }

        // Handling case where no connection was successful
        if (!joined) {
            log.info("No available bootstrap node found. Continuing with the current targetNetworkAddress.");
        }

    }




    @Override
    public synchronized void run() {
        MDC.put("logicalTimestamp", LogicalTimestamp.nextStamp());
        address = new Address(myIP, myPort, nodeId);
        targetNetworkAddress = new Address(leaderNodeIP, leaderPort, leaderId);
        try {
            chatService = startChatService();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }finally {
            // Clear the MDC after use
            MDC.clear();
        }
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
        neighbours = new DSNeighbours();
        communicationHUB = new CommunicationHUB(this);
        topologyServiceRmiProxy = new TopologyServiceRmiProxy(this);
        sendMessageServiceRmiProxy = new SendMessageServiceRmiProxy(this);
        bullyAlgorithm = new BullyAlgorithm(this);
        try {
            initializeCommunicationHUB();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        if (this.address.equals(targetNetworkAddress)) {
            File stateFile = new File("node_state.json");
            if (!stateFile.exists()) {
                writeStateToFile(true); // Write true if it's the first attempt
            } else {
                firstAttempt = readStateFromFile(); // Read the existing state from the file
            }
        }
    }

    public void writeStateToFile(boolean firstAttempt) {
        JSONObject stateObject = new JSONObject();
        stateObject.put("firstAttempt", firstAttempt);
        try (FileWriter file = new FileWriter("C:\\Users\\Acer\\IdeaProjects\\DSVASEM\\src\\main\\resources\\node_state.json")) {
            file.write(stateObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean readStateFromFile() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader("C:\\Users\\Acer\\IdeaProjects\\DSVASEM\\src\\main\\resources\\node_state.json")) {
            JSONObject stateObject = (JSONObject) parser.parse(reader);
            return (Boolean) stateObject.get("firstAttempt");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return true; // Default to true in case of an error
        }
    }



    public static void main(String[] args) {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        if (args.length < 1) {
            System.err.println("Usage: java Main <yourID> <yourIP> <leaderID> <leaderIP>");
            System.exit(1);
        }

        thisNode = new Node(scheduler, args);
        thisNode.run();
    }
    private void startChatCLI(){
        chatCLI.printWelcomeMessage();
        new Thread(chatCLI).start();
    }
}
