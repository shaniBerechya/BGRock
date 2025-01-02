package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBrodcast implements Broadcast {
    private int tick;

    public TickBrodcast(int tick){
        this.tick = tick;
    }

    public int getBrodcast(){
        return tick;
    }
    
}
