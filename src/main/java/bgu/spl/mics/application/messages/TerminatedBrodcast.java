package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminatedBrodcast implements Broadcast{
    private String sender;

    public TerminatedBrodcast(String sender){
        this.sender = sender;
    }

    public String getSender(){
        return sender;
    }
    
}
