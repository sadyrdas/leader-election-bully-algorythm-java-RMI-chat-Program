package cz.cvut.fel.dsva.semestralka.service;

import cz.cvut.fel.dsva.semestralka.base.Message;
import cz.cvut.fel.dsva.semestralka.base.Node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {
    // Method for a node to request sending message
    void sendMessage(int receiverID, String content) throws RemoteException;

    // Method for getting info about commands;
    void help() throws RemoteException;

    //Method for printing nodeStatus;
    void printStatus(Node node) throws RemoteException;

    // Method for a node to request receiving message
    void receiveMessage(Message message) throws  RemoteException;

    // Method for a node to request logIN
    void logIN(Node node) throws RemoteException;

    // Method for a node to logOUT
    void logOUT(Node node) throws RemoteException;

    // Method for a node to request its messages
    List<Message> getMessages(Node node) throws RemoteException;

    // Method for a node to check if there are new messages
    boolean hasNewMessages(Node node) throws RemoteException;

    void registerNode(Node node) throws RemoteException;
}
