package cz.cvut.fel.dsva.semestralka.service;

import cz.cvut.fel.dsva.semestralka.base.Message;
import cz.cvut.fel.dsva.semestralka.base.Node;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ChatServiceImpl extends UnicastRemoteObject implements ChatService{
    private List<Node> nodes;
    private Node node;

    public ChatServiceImpl() throws RemoteException {
        super();
        this.nodes = new ArrayList<>();
    }

    @Override
    public void sendMessage(int receiverID, String content) throws RemoteException {
        if (node.getIsLeader()){
            node.setNodeLeader();
            Message message = new Message(node.getNodeId(), receiverID, content);
            log.info("You send message to node with id {} with content {}", receiverID, content);
            Node recieverNode = node.findNodeByID(receiverID);
            if (recieverNode != null){
                recieverNode.addMsgToList(message);
            }else {
                log.error("Node id must not be null!");
            }
        }
    }

    @Override
    public void help() throws RemoteException {
        log.info("'help' - Available commands ");
        log.info("'send' - Send message to the next node");
        log.info("send <id of target node> <Message>");
        log.info("'status' - Print your status");

    }

    @Override
    public void printStatus(Node node) throws RemoteException {
        log.info("This node's id is" + " " + node.getNodeId() + " " + " with address" + " " + node.getAddress() + " " + "with Neighbors" + " " + node.getNeighbours());
    }

    @Override
    public void receiveMessage(Message message) throws RemoteException {

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
}
