package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    //Fileds:
    int id;
    int frequency;
    List<TrackedObject> lastTrackedObject;
    enum status {
        Up, Down, Eror
    }

    /********************************************* Constrector ***************************************************/
    /**
     * Constructs a LiDar object with the given parameters.
     *
     * @param id              The unique identifier of the LiDar.
     * @param frequency       The frequency at which the LiDar operates.
     */
    public LiDarWorkerTracker(int id,int frequency){
        //TO DO
    }

    /********************************************* Methods ***************************************************/
    /**
     * Prepares the list of {@code lastTrackedObject} using data from the {@link LiDarDataBase}.
     * 
     * @param timeDetected the time at which the object was detected by the camera.
     * @param time the time at which the object is tracked by the {@code LiDarWorkerTracker}.
     */
    public void preparesDate(int timeDetected, int time){
        //TO DO
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
    public TrackedObject getTrackedObject(int timeTracked, int time){
        //TO DO
        return null;
    }

}
