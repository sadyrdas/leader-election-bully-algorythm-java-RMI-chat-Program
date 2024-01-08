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
import java.rmi.server.UnicastRemoteObject;


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
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopChatService));
    }

    public void stopChatService() {
        try {
            UnicastRemoteObject.unexportObject(chatService, true);
            log.info("Chat service stopped successfully.");
        } catch (RemoteException e) {
            log.error("Error stopping chat service: " + e.getMessage());
        }
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


}



