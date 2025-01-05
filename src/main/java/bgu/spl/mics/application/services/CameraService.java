package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBrodcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBrodcast;
import bgu.spl.mics.application.messages.TickBrodcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;


/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    //Fildes:
    Camera camera;
    int clock;
    StatisticalFolder statisticalFolder;
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("camera"+ camera.getId());
        this.camera = camera;
        statisticalFolder = StatisticalFolder.getInstance();
        clock = 0;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBrodcast.class, TickBrodcast -> {
            // Update the clock with the current time from the TickBroadcast
            clock = TickBrodcast.getBrodcast();
            StampedDetectedObjects detectedObjects = null;

            //Chack for eror:
            if(camera.erorDescripion(clock) != null){
                statisticalFolder.setErrorDescription(camera.erorDescripion(clock));
                statisticalFolder.setFaultySensor(this);
                CrashedBrodcast crashedBrodcast = new CrashedBrodcast();
                sendBroadcast(crashedBrodcast);
                terminate();
            }
            // Get detected objects for the current time     
            else if(clock > camera.getFrequency()){
                detectedObjects = camera.getDetectedObject(clock);
                if (detectedObjects != null) {
                    // Create a DetectObjectsEvent and send it
                    DetectObjectsEvent event = new DetectObjectsEvent(detectedObjects);
                    sendEvent(event);
    
                    //Update the StatisticalFolder:
                    statisticalFolder.updateForCamera(getName(), detectedObjects);
                }
                else if (camera.getStatus().equals(STATUS.ERROR)){
                    TerminatedBrodcast terminatedBrodcast = new TerminatedBrodcast("camera");
                    sendBroadcast(terminatedBrodcast);
                }
            }

           
            

            // Handle camera status
            switch (camera.getStatus()) {
                case DOWN:
                    TerminatedBrodcast terminatedBrodcast = new TerminatedBrodcast("camera");
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

       
    }
}
