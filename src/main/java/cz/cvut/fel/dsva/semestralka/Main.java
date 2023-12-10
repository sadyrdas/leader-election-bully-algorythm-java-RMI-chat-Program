package cz.cvut.fel.dsva.semestralka;

import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.Node;

import java.rmi.RemoteException;

public class Main {
    public static void main(String[] args) throws RemoteException {
        Node node1 = new Node(args);
        ChatCLI chatCLI = new ChatCLI(node1);
        chatCLI.run();
    }
}
