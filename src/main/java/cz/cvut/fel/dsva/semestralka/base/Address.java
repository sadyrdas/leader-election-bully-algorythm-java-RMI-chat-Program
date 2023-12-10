package cz.cvut.fel.dsva.semestralka.base;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class Address {
    private String host;
    private int port;
    private Long nodeID;

    public Address(String host, int port){
        this.host = host;
        this.port = port;
    }

    public Address(String host, int port, Long nodeID){
        this.host = host;
        this.port = port;
        this.nodeID = nodeID;
    }


    @Override
    public String toString(){
        return ("Address: " + "host " + host + " " + "port " + port);
    }

    @Override
    public boolean equals(Object object){
        if (object instanceof Address){
            Address address = (Address) object;
            return Objects.equals(address.getHost(), host) &&
                    address.getPort() == port &&
                    Objects.equals(address.getNodeID(), nodeID);
        }
        return false;
    }
}
