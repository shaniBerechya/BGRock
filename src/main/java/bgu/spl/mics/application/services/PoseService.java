package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBrodcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBrodcast;
import bgu.spl.mics.application.messages.TickBrodcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private final GPSIMU gpsimu;
    private final StatisticalFolder statisticalFolder;



    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
        statisticalFolder = StatisticalFolder.getInstance();
    }
    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
   
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBrodcast.class, TickBrodcast -> {
            // Update the GPSIMU time
            gpsimu.setTime(TickBrodcast.getBrodcast());

            // Retrieve the current pose
            Pose currentPose = gpsimu.getPose();

            if (currentPose != null) {
                // Create and send PoseEvent
                statisticalFolder.updateForGPS(currentPose);
                PoseEvent event = new PoseEvent(currentPose);
                sendEvent(event);
            }

            // Handle GPSIMU status
            switch (gpsimu.getStatus()) {
                case DOWN:
                    TerminatedBrodcast terminatedBrodcast = new TerminatedBrodcast("pose");
                    sendBroadcast(terminatedBrodcast);
                    terminate();
                    break;
                default:
                    break;
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
