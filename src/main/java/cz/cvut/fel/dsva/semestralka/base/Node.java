package cz.cvut.fel.dsva.semestralka.base;

import cz.cvut.fel.dsva.semestralka.ChatCLI;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
public class Node {
    private int nodeId;
    private Address address;
    private List<Node> neighbours;
    private Boolean isLeader = false;
    private final ChatCLI chatCLI = new ChatCLI(this);
    private List<Message> messageList;

    public Node(String[] args) throws RemoteException {
        if (args.length == 3){
            this.nodeId = Integer.parseInt(args[0]);
            this.address = new Address((args[1]), Integer.parseInt(args[2]));
        }else {
            throw new IllegalArgumentException("Invalid arguments to input, try again!");
        }
        this.neighbours = new ArrayList<>();
    }
    public void setNodeLeader(){
        this.isLeader = true;
    }
    public void addMsgToList(Message message){
        messageList.add(message);
    }
    public Node findNodeByID(int id){
        for (Node currentNode: neighbours){
            if (currentNode.getNodeId() == id){
                return currentNode;
            }
        }
        log.error("No node with this id was found");
        return null;
    }

    public void addNeighbours(Node neighbour){
        neighbours.add(neighbour);
    }
}
