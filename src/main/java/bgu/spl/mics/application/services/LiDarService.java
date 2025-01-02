package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBrodcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBrodcast;
import bgu.spl.mics.application.messages.TickBrodcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    //Fildes:
    private final LiDarWorkerTracker liDarWorkerTracker;
    private int clock;
    private final StatisticalFolder statisticalFolder;
    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker liDarWorkerTracker) {
        super("LiDarService_" + liDarWorkerTracker.getID());
        this.liDarWorkerTracker = liDarWorkerTracker;
        this.clock = 0;
        this.statisticalFolder = StatisticalFolder.getInstance();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBrodcast.class, TickBroadcast -> {
            clock = TickBroadcast.getBrodcast();

            // Check if it is possible to track objects at the current time
            while (liDarWorkerTracker.isPossibleToTreack(clock)) {
                TrackedObject trackedObject = liDarWorkerTracker.getTrackedObjects(clock);

                if (trackedObject != null) {
                    TrackedObjectsEvent event = new TrackedObjectsEvent(trackedObject);
                    sendEvent(event);

                    // Update statistical folder
                    statisticalFolder.updateForLiDar(getName(), trackedObject);
                }
            }

            // Handle LiDAR status
            switch (liDarWorkerTracker.geStatus()) {
                case DOWN:
                    TerminatedBrodcast terminatedBrodcast = new TerminatedBrodcast("lidar");
                    sendBroadcast(terminatedBrodcast);
                    terminate();
                    break;
                default:
                    break;
            }
        });

        // Subscribe to DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, DetectObjectsEvent -> {
            StampedDetectedObjects stampedDetectedObjects = DetectObjectsEvent. getEvent();
            int timeDetected = stampedDetectedObjects.getTime();
            for (DetectedObject object : stampedDetectedObjects.getDetectedObjects()){
                liDarWorkerTracker.addTrackedObjects(timeDetected, object.getId(), object.getDescription(), clock);
                if(liDarWorkerTracker.geStatus() == STATUS.ERROR){
                    statisticalFolder.setFaultySensor(this);
                    CrashedBrodcast crashedBrodcast = new CrashedBrodcast();
                    sendBroadcast(crashedBrodcast);
                    terminate();
                    break;
                }
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

        // Log initialization
        System.out.println(getName() + " initialized.");
}
}
