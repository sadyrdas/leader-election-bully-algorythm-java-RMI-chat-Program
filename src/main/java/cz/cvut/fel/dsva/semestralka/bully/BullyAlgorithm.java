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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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
        log.info("Nodes with highest id: {}", otherNodes);
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

        Runnable delayedTask = () -> {
            try {
                ChatService service = myNode.getCommunicationHUB().getRMIProxy(address);
                service.sendElectionMsg(senderId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        };

        long delay = 3; // Delay in seconds
        myNode.getScheduler().schedule(delayedTask, delay, TimeUnit.SECONDS);
    }



    public void sendElectionProxy(long senderId) {
        List<Address> otherNodes = findAllNodesWithHigherID(senderId);
        Address address = myNode.getNeighbours().getAddressById((int) senderId);
        if (otherNodes.size() == 1 && otherNodes.contains(address)) {
            log.info("No other pretends to be a leader");
        } else {
            for (Address otherNode : otherNodes) {
                Runnable task = () -> {
                    try {
                        ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                        otherNodeService.receiveMessage("Leader is out, i will start election", senderId);
                        otherNodeService.sendResponseForStartingElection(senderId, otherNode.getNodeID());
                        ChatService sender = myNode.getCommunicationHUB().getRMIProxy(address);
                        sender.logInfo("My work is done");
                    } catch (RemoteException e) {
                        log.info("Node is dead with id {}", otherNode.nodeID);
                        myNode.getNeighbours().removeNode(otherNode);
                        myNode.getNeighbours().addNewNode(address);
                        ChatService myNodeService = null;
                        try {
                            myNodeService = myNode.getCommunicationHUB().getRMIProxy(myNode.getAddress());
                        } catch (RemoteException ex) {
                            throw new RuntimeException(ex);
                        }
                        try {
                            myNodeService.electionByLeaderAgain(address, otherNode);
                            myNode.getNeighbours().removeNode(otherNode);
                            myNodeService.logInfo("I am new leader");
                        } catch (RemoteException ex) {
                            throw new RuntimeException(ex);
                        }
                        log.info("I am new leader");
                    }
                };
                long delay = 7; // Delay in seconds, adjust as needed
                myNode.getScheduler().schedule(task, delay, TimeUnit.SECONDS);
            }
        }
    }


    public void startElectionAgain(long senderId){
        List<Address> otherNodes = findAllNodesWithHigherID(senderId);
        Address sender = myNode.getNeighbours().getAddressById((int) senderId);
        for (Address otherNode : otherNodes) {
            Runnable task = () -> {
                try {
                    ChatService otherNodeService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                    otherNodeService.startElectionAgain(otherNodes);
                } catch (RemoteException e) {
                    myNode.getNeighbours().removeNode(otherNode);
                    ChatService myNodeService = null;
                    try {
                        myNodeService = myNode.getCommunicationHUB().getRMIProxy(myNode.getAddress());
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    try {
                        myNodeService.electionByLeaderAgain(myNode.getAddress(), otherNode);
                        myNode.getNeighbours().removeNode(otherNode);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            long delay = 9; // Delay in seconds, adjust as needed
            myNode.getScheduler().schedule(task, delay, TimeUnit.SECONDS);
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
            Runnable task = () -> {
                try {
                    ChatService chatService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                    chatService.receiveMessage(message, address.getNodeID());
                } catch (RemoteException e) {
                    log.error("Couldn't get responses");
                }
            };
            long delay = 4; // Delay in seconds, adjust as needed
            myNode.getScheduler().schedule(task, delay, TimeUnit.SECONDS);
        }

        // Scheduling the setting of a new leader
        Runnable leaderTask = () -> {
            try {
                ChatService futureLeaderService = myNode.getCommunicationHUB().getRMIProxy(address);
                futureLeaderService.logInfo("I am Node with highest id. Now i will become leader and repair topology");
                futureLeaderService.election(address, leaderNode);
            } catch (RemoteException e) {
                log.error("Couldn't get responses");
            }
        };
        long delay = 4;
        myNode.getScheduler().schedule(leaderTask, delay, TimeUnit.SECONDS);
    }

    public void startElectionByLeaderAgain(Address sender) {
        Address address = myNode.getNeighbours().getAddressById(5);

        // Assuming getNeighbours() returns a List<Address>, create a CopyOnWriteArrayList from it
        List<Address> otherNodes = new CopyOnWriteArrayList<>(myNode.getNeighbours().getNeighbours());

        for (Address otherNode : otherNodes) {
            try {
                ChatService chatService = myNode.getCommunicationHUB().getRMIProxy(otherNode);
                chatService.electionByLeaderAgain(address, sender);
                chatService.notifyAboutNewLeader(address);
            } catch (RemoteException e) {
                log.error("Couldn't get responses");
            }
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
