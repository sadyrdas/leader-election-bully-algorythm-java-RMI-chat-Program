package cz.cvut.fel.dsva.semestralka.service;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
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
            dsNeighbours.setLeaderNode(addr);
            return dsNeighbours;
        } else {
            log.info("Someone is joining");
            log.info("Added node with address {}", addr);
            myNode.getTopologyServiceRmiProxy().repairTopologyAfterJoin(addr);
            myNode.getTopologyServiceRmiProxy().notifyOtherNodes();
            log.info("Updating topology and send updated topology to otherNodes");
            log.info("Neighbours after JOIN: {}", dsNeighbours);
            return dsNeighbours;
        }
    }


    @Override
    public void notifyAboutUpdatedNeighbors(List<Address> addressNeighbours) throws RemoteException {
        myNode.getTopologyServiceRmiProxy().showCurrentTopology(addressNeighbours);
    }

    @Override
    public void repairTopologyAfterJoin(Address address) throws RemoteException {
        DSNeighbours dsNeighbours = myNode.getNeighbours();
        dsNeighbours.addNewNode(address);
        dsNeighbours.setLeaderNode(myNode.getNeighbours().getLeaderNode());
        myNode.setNeighbours(dsNeighbours);
    }

    @Override
    public void repairTopologyAfterLogOut(int nodeId) throws RemoteException {
        DSNeighbours dsNeighbours = myNode.getNeighbours();
        dsNeighbours.removeNodeById(nodeId);
        dsNeighbours.setNeighbours(myNode.getNeighbours().getNeighbours());
        myNode.setNeighbours(dsNeighbours);
    }


    @Override
    public void sendMessage(long senderId, int receiverId, String message) throws RemoteException {
        myNode.getSendMessageServiceRmiProxy().notifyLeaderAboutSendMessage(senderId, receiverId, message);
        myNode.getSendMessageServiceRmiProxy().broadCastMessage(senderId,receiverId, message);
    }




    @Override
    public void help() throws RemoteException {
        log.info("'help' - Available commands ");
        log.info("'send' - Send message to the next node");
        log.info("send <id of target node> <Message>");
        log.info("'status' - Print your status");
        log.info("'neighbours' - Show list of neighbours");
        log.info("'logout' - Log out from chat" );

    }
    @Override
    public void notifyLeaderAboutSendMessage(long senderId, long receiverId, String message) throws RemoteException {
        // Log the event or perform other actions based on different notifications
        log.info("Received notification about sending message from node {} to node {} with content {}", senderId, receiverId, message );
        log.info("Send response with status of message to Node {}", senderId);
    }

    @Override
    public void notifyNodeAboutSentMessage(long senderId, long receiverId, String message) throws RemoteException{
        if (!myNode.getBullyAlgorithm().isLeaderAlive()){
            log.info("Your message to Node with id {} was sent with status {} Please check if receiver is active and retry later", receiverId, message);
        }else {
            log.info("Your message to Node with id {} was sent with status {}", receiverId, message);
        }
    }

    @Override
    public void printStatus(Node node) throws RemoteException {
        log.info("This node's id is" + " " + node.getNodeId() + " " + " with address" + " " + node.getAddress() + " " + "\nwith Neighbors" + " " + node.getNeighbours());
    }

    @Override
    public void receiveMessage(String message, long senderId) throws RemoteException {
        if (message.startsWith("Node") && message.contains("has logged out")) {
            handleLogoutMessage(message);
        } else {
            log.info("Received message: {} from {}", message, senderId);
        }
    }

    @Override
    public void checkStatusOfLeader(long senderId) throws RemoteException {
        myNode.getBullyAlgorithm().broadCastStatusCheckOfLeader(senderId);
    }

    @Override
    public void sendResponseAboutLeader() throws RemoteException{
        if (!myNode.getBullyAlgorithm().isLeaderAlive()){
            log.info("Leader is out ;(");
        }else {
            log.info("Leader is alive!");
        }
    }

    @Override
    public void sendElectionMsg(long senderId) throws RemoteException {
        log.info("I will start election");
        myNode.getBullyAlgorithm().sendElectionProxy(senderId);
        log.info("My work is done");
        myNode.getBullyAlgorithm().startElectionAgain(senderId);
    }


    @Override
    public void startElectionAgain(List<Address> highestPriorityNodes) throws RemoteException{
        log.info("Started election again!");
        myNode.getBullyAlgorithm().setFutureLeader(highestPriorityNodes);
    }

    @Override
    public void logInfo(String info) throws RemoteException{
        log.info(info);
    }

    @Override
    public void election(Address address) throws RemoteException {
        DSNeighbours dsNeighbours = myNode.getNeighbours();
        dsNeighbours.setLeaderNode(address);
        myNode.getBullyAlgorithm().setLeaderAlive(true);
        myNode.getSendMessageServiceRmiProxy().setSentMessageStatus(true);
        myNode.setNeighbours(dsNeighbours);
        myNode.getTopologyServiceRmiProxy().repairTopologyWithNewLeader(address);
        myNode.getTopologyServiceRmiProxy().notifyOtherNodes();
    }

    @Override
    public void repairTopologyWithNewLeader(List<Address> addresses, Address address) throws RemoteException {
        DSNeighbours dsNeighbours = myNode.getNeighbours();
        myNode.getCommunicationHUB().setRmiProxy(address);
        dsNeighbours.setLeaderNode(address);
        myNode.getBullyAlgorithm().setLeaderAlive(true);
        myNode.getCommunicationHUB().setRmiProxy(address);
        myNode.setNeighbours(dsNeighbours);
    }

    @Override
    public synchronized void changeRmi(Address address) throws RemoteException {
        myNode.setTargetNetworkAddress(address.host, address.port);
        myNode.getCommunicationHUB().setRmiProxy(address);
    }

    @Override
    public Address getCurrentLeader() throws RemoteException {
        return myNode.getNeighbours().getLeaderNode();
    }

    @Override
    public void sendResponseForStartingElection(long senderId, Long receiverId) throws RemoteException {
        myNode.getBullyAlgorithm().getResponseForStartingElection(senderId, receiverId);
    }


    private void handleLogoutMessage(String message) throws RemoteException {
        // Extract node ID from the logout message and update the neighbors accordingly
        // Example: "Node 123 has logged out."
        String[] parts = message.split(" ");
        if (parts.length >= 2) {
            int nodeId = Integer.parseInt(parts[1]);
            myNode.getTopologyServiceRmiProxy().repairTopologyAfterLogOut(nodeId);
            myNode.getTopologyServiceRmiProxy().notifyOtherNodes();
            log.info("Node {} has logged out.", nodeId);
            log.info("Updating topology and send updated topology to otherNodes");
        }
    }



    @Override
    public void logOUT(Address address) throws RemoteException {
        log.info("Started LogOut");
        if (myNode.getAddress().compareTo(myNode.getNeighbours().getLeaderNode()) == 0){
            System.out.println("You are leader");
            System.out.println("Your absence can be without consequences. I will find new leader.");
            myNode.getTopologyServiceRmiProxy().broadcastLogout();
            myNode.getChatCLI().stop();
        }
        myNode.getTopologyServiceRmiProxy().broadcastLogout();
        myNode.getChatCLI().stop();
    }



    @Override
    public String getAddressesOfNeighbours() throws RemoteException {
        DSNeighbours activeNeighbours = myNode.getNeighbours();
        if (!activeNeighbours.getNeighbours().isEmpty()) {
            log.info("Your neighbours {} \n Leader {}", activeNeighbours.getNeighbours(), activeNeighbours.getLeaderNode());
            return "Your neighbours: " + activeNeighbours.getNeighbours();
        } else {
            log.warn("No active neighbours found.");
            return "No active neighbours found.";
        }
    }
}
