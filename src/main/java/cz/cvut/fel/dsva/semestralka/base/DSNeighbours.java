package cz.cvut.fel.dsva.semestralka.base;



import cz.cvut.fel.dsva.semestralka.Node;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
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
        log.info(neighbours.toString());
        log.error("Couldn't find address with id {}", id);
        return null;
    }



    public boolean isLeaderPresent() {
        return leaderNode != null;
    }
    public void addNewNode( Address address) {
        if (address.getPort() > 0 && !address.getHost().isEmpty()) {
            neighbours.add(address);
            log.info("Adding new Node{hostname:{}, port:{}, nodeId:{}}",
                    address.getHost(), address.getPort(), address.getNodeID());
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
