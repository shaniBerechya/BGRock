package bgu.spl.mics.application.objects;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    //Fileds:
    int id;
    int frequency;
    Queue<TrackedObject> lastTrackedObject;
    private STATUS status;
    private LiDarDataBase dataBase;

    /********************************************* Constrector ***************************************************/
    /**
     * Constructs a LiDar object with the given parameters.
     *
     * @param id              The unique identifier of the LiDar.
     * @param frequency       The frequency at which the LiDar operates.
     */
        public LiDarWorkerTracker(int id, int frequency, String dataBaseFilePath) {
        this.id = id;
        this.frequency = frequency;
        this.lastTrackedObject = new ArrayDeque<TrackedObject>();
        this.status = STATUS.UP;
        this.dataBase = LiDarDataBase.getInstance(dataBaseFilePath); // Load the singleton database
    }
    

    /********************************************* Methods ***************************************************/
    /**
     * Prepares the list of {@code lastTrackedObject} using data from the {@link LiDarDataBase}.
     * 
     * @param timeDetected the time at which the object was detected by the camera.
     * @param id
     */
    public void addTrackedObjects(int timeDetected, String id, String description, int currentTime){
        StampedCloudPoints stampedCoordinates = dataBase.getStampedCloudPoints(timeDetected, id);
        if(stampedCoordinates != null){
            List<CloudPoint> coordinates = stampedCoordinates.getCloudPoints();
            TrackedObject trackedObject  = new TrackedObject(id, currentTime,timeDetected, description,coordinates);
            lastTrackedObject.add(trackedObject);

        }
        else {
            status = STATUS.ERROR;
        }
    }
    /**
    * @return the {@code frequency}
    */
    public int getFrequency(){
        return frequency;
    }

    /**
     * Retrieves the detected object from the {@code TrackedObject} that matches the calculated time limit: {@code time} - {@code frequency}.
     *
     * @param timeTracked the time at which the object was detected by the LiDar.
     * @param time the time at which the object is tracked by the {@code LiDarWorkerTracker}.
     * @return the {@code TrackedObject} corresponding to the calculated time, or {@code null} if no match is found.
     */
    public TrackedObject getTrackedObjects (int time){
        if(dataBase.isFinished() && lastTrackedObject.isEmpty()){
            status = STATUS.DOWN;
            return null;
        }
        else {
            TrackedObject trackedObject = lastTrackedObject.peek();
            if (trackedObject != null  && trackedObject.getTime() <= time - frequency){
                return lastTrackedObject.poll();
            }
        }
        return null;
    }
    public boolean isPossibleToTreack(int time){
        return (!lastTrackedObject.isEmpty());
    }

    public STATUS geStatus(){
        return this.status;
    }

    public int getID(){
        return this.id;
    }

    public Queue<TrackedObject> getLastTrackedObject(){
        return lastTrackedObject;
    }
}
