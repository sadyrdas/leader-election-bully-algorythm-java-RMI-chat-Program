package cz.cvut.fel.dsva.semestralka.base;


import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class Address implements Comparable<Address>, Serializable {
    public String host;
    public Integer port;
    public Long nodeID;
    private boolean isOnline = true;


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
        return ("Address: " + "nodeId: " + nodeID + " " + "host: " + host + " " + "port: " + port);
    }


    @Override
    public boolean equals(Object object){
        if (object instanceof Address){
            Address address = (Address) object;
            return Objects.equals(address.getHost(), host) &&
                    Objects.equals(address.getPort(), port) &&
                    Objects.equals(address.getNodeID(), nodeID);
        }
        return false;
    }



    @Override
    public int compareTo(Address address) {
        int retval;
        if ((retval = host.compareTo(address.getHost())) == 0 ) {
            if ((retval = port.compareTo(address.getPort())) == 0 ) {
                return 0;
            }
            else
                return retval;
        }
        else
            return retval;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("host", this.host);
        jsonObject.put("port", this.port);
        jsonObject.put("nodeID", this.nodeID);
        return jsonObject;
    }

    public static Address fromJson(JSONObject jsonObject) {
        String host = (String) jsonObject.get("host");
        Integer port = ((Long) jsonObject.get("port")).intValue(); // Cast to Long then to Integer
        Long nodeID = (Long) jsonObject.get("nodeID");

        Address address = new Address(host, port, nodeID);
        return address;
    }
}
