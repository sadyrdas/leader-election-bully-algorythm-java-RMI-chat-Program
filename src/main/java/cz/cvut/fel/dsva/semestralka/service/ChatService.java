package cz.cvut.fel.dsva.semestralka.service;

import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.base.Message;
import cz.cvut.fel.dsva.semestralka.Node;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {

    DSNeighbours join(Address add) throws RemoteException;

    void updateNeighbors(DSNeighbours updatedNeighbors) throws RemoteException;


    // Method for a node to request sending message
    void sendMessage(int receiverID, String content) throws RemoteException;

    // Method for getting info about commands;
    void help() throws RemoteException;

    //Method for printing nodeStatus;
    void printStatus(Node node) throws RemoteException;

    // Method for a node to request receiving message
    void receiveMessage(String msg) throws  RemoteException;

    void notifyLeaderAboutEvent(String event) throws RemoteException;

    // Method for a node to logOUT
    void logOUT() throws RemoteException;

    // Method for a node to request its messages
    List<Message> getMessages(Node node) throws RemoteException;

    // Method for a node to check if there are new messages
    boolean hasNewMessages(Node node) throws RemoteException;

    void registerNode(Node node) throws RemoteException;

    void sendHello(int receiverId) throws RemoteException;

    void broadcastMessage(String message) throws RemoteException;
    String getAddressesOfNeighbours() throws RemoteException;

    void updateNeighborsList(Address newNeighbor, boolean join) throws RemoteException;
}
