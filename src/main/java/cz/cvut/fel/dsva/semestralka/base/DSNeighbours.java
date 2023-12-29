package cz.cvut.fel.dsva.semestralka.base;



import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Getter
@Setter
public class DSNeighbours implements Serializable {
    private List<Address> neighbours;
    private Address leaderNode;
    public DSNeighbours(Address initialNode) {
        this.neighbours = new ArrayList<>();
        this.leaderNode = initialNode;
    }


    public Address getAddressById(int id) {
        for (Address address : neighbours){
            if (address.getNodeID() == id){
                return address;
            }
        }
        log.error("Couldn't find address with id {}", id);
        return null;
    }
    public Long getNodeIdFromAddress(Address address) {
        for (Address neighbour : neighbours) {
            if (neighbour.equals(address)) {
                // Assuming Address class has a getNodeId() method
                return neighbour.getNodeID();
            }
        }
        // If the address is not found, return some default value or handle accordingly
        return -1L; // Replace with an appropriate default or error handling
    }

    public void removeNodeById(int nodeId) {
        Iterator<Address> iterator = neighbours.iterator();
        while (iterator.hasNext()) {
            Address address = iterator.next();
            if (getNodeIdFromAddress(address) == nodeId) {
                iterator.remove(); // Use iterator's remove method
                break; // Break the loop after removing the element
            }
        }
    }



    public boolean isLeaderPresent() {
        return leaderNode != null;
    }
    public void addNewNode( Address address) {
        if (address.getPort() > 0 && !address.getHost().isEmpty()) {
            neighbours.add(address);
        }
    }

    public void removeNode(Address address){
        if (neighbours.contains(address)){
            neighbours.remove(address);
        }
    }
    @Override
    public String toString() {
        return "BullyNodeInfo {" +
                ", neighbors=" + neighbours +
                ",\n leader=" + leaderNode +
                '}';
    }
}
