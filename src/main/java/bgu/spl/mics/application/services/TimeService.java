package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBrodcast;
import bgu.spl.mics.application.messages.TerminatedBrodcast;
import bgu.spl.mics.application.messages.TickBrodcast;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    // Fields:
    private final int tickTime; // Duration of each tick in milliseconds
    private final int duration; // Total number of ticks
    private int currentTick; // The current tick count

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int tickTime, int duration) {
        super("TimeService");
        this.tickTime = tickTime;
        this.duration = duration;
        this.currentTick = 0;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBrodcast.class, TickBrodcast -> {
            try{
                Thread.sleep(this.tickTime * 1000);
            } catch (InterruptedException e){}

            currentTick = currentTick + 1 ;
            if (currentTick <= duration){
                TickBrodcast tickBrodcast = new TickBrodcast(currentTick);
                sendBroadcast(tickBrodcast);
            }
            else{
                TerminatedBrodcast terminatedBrodcast = new TerminatedBrodcast("time");
                sendBroadcast(terminatedBrodcast);
            }
            
        });

        subscribeBroadcast(TerminatedBrodcast.class, TerminatedBrodcast -> {
            if(TerminatedBrodcast.getSender() == "fusion"){
                terminate();
            }
        });

        subscribeBroadcast(CrashedBrodcast.class, CrashedBrodcast -> {
            terminate();
        });
        
    }
}
