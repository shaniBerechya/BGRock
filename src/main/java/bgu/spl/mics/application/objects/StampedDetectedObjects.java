package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    //Fileds:
    int time;
    List<DetectedObject> detectedObjects;

    /********************************************* Constrector ***************************************************/
    public StampedDetectedObjects(int time){

    }

    /********************************************* Methods ***************************************************/
    /**
     * Adds a {@code DetectedObject} to the list of detected objects if its timestamp matches {@code this.time}.
     *
     * @param detectedObject the object to add, where {@code detectedObject.time} equals {@code this.time}.
     * @post The {@code detectedObject} is added to {@code detectedObjects}.
     */
    public void addDetectedObject(DetectedObject detectedObject){
        //TO DO
    }

    /**
     * Retrieves the list of objects detected at the specified timestamp.
     * 
     *  @return {@code detectedObjects}.
     */
    public List<DetectedObject> getDetectedObjects(){
        return detectedObjects;
    }
}
