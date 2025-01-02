package bgu.spl.mics.application.objects;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    //Fileds:
    private String id;
    private int time;//the time it was treckd by the lidar
    private int timeDetected;
    private String description;
    private List<CloudPoint> coordinates;

    /********************************************* Constrector ***************************************************/
    public TrackedObject(String id, int time, int timeDetected, String description,List<CloudPoint> coordinates ){
        this.id = id;
        this.time = time;
        this.timeDetected = timeDetected;
        this.description = description;
        this.coordinates = coordinates;
    }
    /********************************************* Methods ***************************************************/

        public String getId() {
            return id;
        }
    
        public int getTime() {
            return time;
        }
    
        public String getDescription() {
            return description;
        }
    
        public List<CloudPoint> getCoordinates() {
            return coordinates;
        }

        public int getTimeDetected() {
            return timeDetected;
        }

}
