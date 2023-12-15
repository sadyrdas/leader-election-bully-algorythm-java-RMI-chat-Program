package cz.cvut.fel.dsva.semestralka.bully;

import cz.cvut.fel.dsva.semestralka.Node;
import cz.cvut.fel.dsva.semestralka.base.Address;
import cz.cvut.fel.dsva.semestralka.base.DSNeighbours;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;

@Slf4j
@Getter
@Setter
public class BullyAlgorithm{
    private Node node;
    private DSNeighbours dsNeighbours;
    public BullyAlgorithm(Node node) {
        this.node = node;
        this.dsNeighbours = node.getNeighbours();
    }
    public Address findHighestPriorityNode(){
        Address address = dsNeighbours.getNeighbours().stream()
                .max(Comparator.comparingLong(Address::getNodeID))
                .orElse(null);
        dsNeighbours.setLeaderNode(address);
        log.info("New leader in our chat is: {}", address);
        return address;
    }

}
