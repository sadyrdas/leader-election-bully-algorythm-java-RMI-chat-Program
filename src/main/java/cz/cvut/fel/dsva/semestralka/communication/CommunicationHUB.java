package cz.cvut.fel.dsva.semestralka.communication;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.service.ChatService;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@Getter
@Setter
@Slf4j
public class CommunicationHUB {
    private Address myAddress;
    private DSNeighbours activeNeighbours;
    private ChatService chatService;
    public CommunicationHUB (Node node) {
        this.myAddress = node.getAddress();
        this.activeNeighbours = node.getNeighbours();
        this.chatService = node.getChatService();

    }

    public ChatService getRMIProxy(Address address) throws RemoteException {
        System.setProperty("java.rmi.server.hostname", address.host);
        if (address.compareTo(myAddress) == 0) return chatService;
        else {
            try {
                Registry registry = LocateRegistry.getRegistry(address.host, address.port);
                return (ChatService) registry.lookup(Node.nameRMI);
            } catch (NotBoundException nbe) {
                log.error("Failed to get RMI proxy: "+ nbe.getMessage());
                throw new RemoteException();
            }
        }

    }
}
