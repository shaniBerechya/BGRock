package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBrodcast  implements Broadcast {
    private boolean crashed;

    public CrashedBrodcast(){
        crashed = true;
    }

    public boolean getBrodcust(){
        return crashed;
    }

    
}
