package bgu.spl.mics.application.services;

import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBrodcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBrodcast;
import bgu.spl.mics.application.messages.TickBrodcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    //Fildes:
    FusionSlam fusionSlam;
    AtomicInteger counterOfSensores;
    private final StatisticalFolder statisticalFolder;


    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("fusionSlam");
        this.fusionSlam = fusionSlam;
        counterOfSensores = new AtomicInteger(fusionSlam.getNumOfSensore());
        statisticalFolder = StatisticalFolder.getInstance();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
     @Override
    protected void initialize() {
        // Handle PoseEvent
        subscribeEvent(PoseEvent.class, PoseEvent -> {
            Pose pose = PoseEvent.getEvent();
            fusionSlam.addPose(pose);
        });

        // Handle TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, TrackedObjectsEvent -> {
            TrackedObject trackedObject = TrackedObjectsEvent.getEvent();
            fusionSlam.addTrackedObject(trackedObject);
        });

        // Handle TickBroadcast
        subscribeBroadcast(TickBrodcast.class, TickBrodcast -> {
            System.out.println("Tick " + TickBrodcast.getBrodcast() + " received by FusionSlamService.");
        });

        // Handle TerminatedBrodcast
        subscribeBroadcast(TerminatedBrodcast.class, TerminatedBrodcast -> {
            if(TerminatedBrodcast.getSender() == "time"){
                TerminatedBrodcast terminatedBrodcast = new TerminatedBrodcast("fusion");
                sendBroadcast(terminatedBrodcast);
            }
            else if (TerminatedBrodcast.getSender() != "fusion"){
                System.out.println("counterOfSensores: " + counterOfSensores +" terminet by: "+ TerminatedBrodcast.getSender());
                counterOfSensores.decrementAndGet();
                if(counterOfSensores.get() == 0){
                    TerminatedBrodcast terminatedBrodcast = new TerminatedBrodcast("fusion");
                    sendBroadcast(terminatedBrodcast);
                }
            }
            else{
                statisticalFolder.setLandMarks(fusionSlam.getLandmarks());
                terminate();
            }
        });

        // Handle TerminatedBrodcast
        subscribeBroadcast(CrashedBrodcast.class, CrashedBrodcastnull -> {
            statisticalFolder.setLandMarks(fusionSlam.getLandmarks());
            terminate();
        });

        // Log initialization
        System.out.println(getName() + " initialized.");
    }
    //metohd to hendl AtomicInteger
    private void subtractOne() {
        for(;;){
            int current = counterOfSensores.get();
            int plusOne = current - 1;
            if (counterOfSensores.compareAndSet(current, plusOne)){
                break;
            }
        }
    }

}
