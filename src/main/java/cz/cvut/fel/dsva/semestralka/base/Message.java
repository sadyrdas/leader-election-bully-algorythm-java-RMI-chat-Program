package cz.cvut.fel.dsva.semestralka.base;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private int receiverID;
    private String msg;

    public Message(int receiverID, String msg){
        this.receiverID = receiverID;
        this.msg = msg;
    }

}
