package cz.cvut.fel.dsva.semestralka.base;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private int senderID;
    private int receiverID;
    private String msg;

    public Message(int senderID, int receiverID, String msg){
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.msg = msg;
    }


}
