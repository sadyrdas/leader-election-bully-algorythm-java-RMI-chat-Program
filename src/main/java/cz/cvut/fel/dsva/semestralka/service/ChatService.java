package cz.cvut.fel.dsva.semestralka.service;

import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.Node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {

    DSNeighbours join(Address add) throws RemoteException;

    void notifyAboutUpdatedNeighbors(List<Address> addresses) throws RemoteException;

    void repairTopologyAfterJoin(Address address) throws RemoteException;

    void repairTopologyAfterLogOut(int nodeID) throws RemoteException;

    // Method for a node to request sending message
    void sendMessage(long senderId, int receiverID, String content) throws RemoteException;

    // Method for getting info about commands;
    void help() throws RemoteException;
    void changeRmi(Address address) throws RemoteException;

    //Method for printing nodeStatus;
    void printStatus(Node node) throws RemoteException;

    // Method for a node to request receiving message


    void notifyLeaderAboutSendMessage(long senderId, long receiverId, String message) throws RemoteException;

    void notifyNodeAboutSentMessage(long senderId, long receiverId, String message) throws RemoteException;

    // Method for a node to logOUT
    void logOUT(Address address) throws RemoteException;


    String getAddressesOfNeighbours() throws RemoteException;

    void repairTopologyWithNewLeader(List<Address> addresses, Address address) throws RemoteException;

    void receiveMessage(String message,  long receiverId) throws RemoteException;

    void checkStatusOfLeader(long senderId) throws RemoteException;
    Address getCurrentLeader() throws RemoteException;

    void sendElectionMsg(long senderId) throws RemoteException;
    void startElectionAgain(List<Address> highestPriorityNodes) throws RemoteException;
    void logInfo(String info) throws RemoteException;
    void election(Address address) throws RemoteException;
    void sendResponseForStartingElection(long senderId, Long receiverId) throws RemoteException;

    void sendResponseAboutLeader() throws RemoteException;
}
