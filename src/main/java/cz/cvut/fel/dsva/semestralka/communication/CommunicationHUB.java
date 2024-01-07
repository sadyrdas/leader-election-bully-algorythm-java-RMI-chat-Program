package cz.cvut.fel.dsva.semestralka.communication;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.service.ChatService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;



@Slf4j
@Getter
@Setter
public class CommunicationHUB {
    private Address myAddress;
    private ChatService chatService;
    private ChatService currentLeaderRmiProxy;
    private Node node;

    public CommunicationHUB(Node node) {
        this.myAddress = node.getAddress();
        this.chatService = node.getChatService();
        this.node = node;
    }


    public synchronized ChatService getRMIProxy(Address address) throws RemoteException {
        System.setProperty("java.rmi.server.hostname", node.getTargetNetworkAddress().host);
        if (address.compareTo(myAddress) == 0) return chatService;
        else {
            try {
                Registry registry = LocateRegistry.getRegistry(address.host, address.port);
                return (ChatService) registry.lookup(Node.nameRMI);
            } catch (NotBoundException nbe) {
                log.error("Failed to get RMI proxy: " + nbe.getMessage());
                throw new RemoteException();
            }
        }
    }

    public synchronized void setRmiProxy(Address newLeaderAddress) {
        try {
            if (newLeaderAddress.compareTo(myAddress) == 0) {
                // If the new leader is the current node itself
                currentLeaderRmiProxy = chatService;
            } else {
                Registry registry = LocateRegistry.getRegistry(newLeaderAddress.host, newLeaderAddress.port);
                currentLeaderRmiProxy = (ChatService) registry.lookup(Node.nameRMI);
            }
        } catch (RemoteException | NotBoundException e) {
            log.error("Failed to set RMI proxy: " + e.getMessage());
            // Handle the exception as needed
        }
    }
}



