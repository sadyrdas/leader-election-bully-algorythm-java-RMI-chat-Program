package cz.cvut.fel.dsva.semestralka.service;

import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import cz.cvut.fel.dsva.semestralka.Node;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface ChatService extends Remote {
    void notifyAboutJoin(Address address) throws RemoteException;
    void notifyAboutLogOut(Address address) throws RemoteException;
    void notifyAboutNewLeader(Address address) throws RemoteException;
    DSNeighbours join(Address add) throws RemoteException;

    void repairTopologyAfterJoin(Address address) throws RemoteException;
    void repairTopologyAfterLogOut(int nodeID) throws RemoteException;
    void sendMessage(long senderId, int receiverID, String content) throws RemoteException;
    void help() throws RemoteException;
    void printStatus(Node node) throws RemoteException;
    void notifyLeaderAboutSendMessage(long senderId, long receiverId, String message) throws RemoteException;
    void notifyNodeAboutSentMessage(long senderId, long receiverId, String message) throws RemoteException;
    void logOUT() throws IOException, ParseException;
    void logOUTForce() throws IOException, ParseException;
    void getTopology(Address address) throws RemoteException;
    void repairTopologyWithNewLeader(List<Address> addresses, Address address) throws RemoteException;
    void receiveMessage(String message,  long receiverId) throws RemoteException;
    void checkStatusOfLeader(long senderId) throws RemoteException;
    Address getCurrentLeader() throws RemoteException;
    void notifyLeaderAboutJoin(Address address) throws RemoteException;
    void sendElectionMsg(long senderId) throws RemoteException;
    void startElectionAgain(List<Address> highestPriorityNodes) throws RemoteException;
    void logInfo(String info) throws RemoteException;
    void election(Address address,  Address loggedOut) throws RemoteException;
    void sendResponseForStartingElection(long senderId, Long receiverId) throws RemoteException;
    void sendResponseAboutLeader() throws RemoteException;

}
