package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    //Fileds:
    private int time;
    private List<DetectedObject> detectedObjects;

    /********************************************* Constrector ***************************************************/
    public StampedDetectedObjects(int time) {
        this.time = time;
        this.detectedObjects = new ArrayList<>();
    }

    /********************************************* Methods ***************************************************/
    /**
     * Adds a {@code DetectedObject} to the list of detected objects if its timestamp matches {@code this.time}.
     *
     * @param detectedObject the object to add, where {@code detectedObject.time} equals {@code this.time}.
     * @post The {@code detectedObject} is added to {@code detectedObjects}.
     */
    public void addDetectedObject(DetectedObject detectedObject){
      if (detectedObject != null) {
            detectedObjects.add(detectedObject);
        }
    }

    /**
     * Retrieves the list of objects detected at the specified timestamp.
     * 
     *  @return {@code detectedObjects}.
     */
    public List<DetectedObject> getDetectedObjects(){
        return detectedObjects;
    }

    public int getTime(){
        return time;
    }
    public String erorDescripion(){
        for(DetectedObject object : detectedObjects){
            String id =  object.getId();
            if(id.equals("ERROR")) {
                return object.getDescription();
            }
        }
        return null;
    }
}
